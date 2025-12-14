package com.dang.identityservice.application.service;

import com.dang.identityservice.application.dto.auth.*;
import com.dang.identityservice.application.exception.AppException;
import com.dang.identityservice.application.exception.ErrorCode;
import com.dang.identityservice.application.port.ClockPort;
import com.dang.identityservice.application.port.JwtProvider;
import com.dang.identityservice.application.port.PasswordHasher;
import com.dang.identityservice.application.port.TokenRevocationStore;
import com.dang.identityservice.domain.model.aggregates.Permission;
import com.dang.identityservice.domain.model.aggregates.RefreshToken;
import com.dang.identityservice.domain.model.aggregates.Role;
import com.dang.identityservice.domain.model.aggregates.User;
import com.dang.identityservice.domain.model.valueobjects.Email;
import com.dang.identityservice.domain.model.valueobjects.PermissionId;
import com.dang.identityservice.domain.model.valueobjects.Username;
import com.dang.identityservice.domain.repository.PermissionRepository;
import com.dang.identityservice.domain.repository.RefreshTokenRepository;
import com.dang.identityservice.domain.repository.RoleRepository;
import com.dang.identityservice.domain.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.dang.identityservice.domain.model.valueobjects.PasswordHash;

import java.util.Objects;
import java.time.Instant;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AuthApplicationService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PermissionRepository permissionRepository;
    private final RefreshTokenRepository refreshTokenRepository;

    private final PasswordHasher passwordHasher;
    private final JwtProvider jwtProvider;
    private final TokenRevocationStore revocationStore;
    private final ClockPort clock;

    @Transactional
    public LoginResponse login(LoginRequest req) {
        Username username = Username.of(req.getUsername());
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_EXISTED));

        if (!user.canLogin()) throw new AppException(ErrorCode.USER_INACTIVE);
        if (!passwordHasher.matches(req.getPassword(), user.getPasswordHash().value())) {
            throw new AppException(ErrorCode.UNAUTHENTICATED);
        }

        Authorities authz = loadAuthorities(user);

        JwtProvider.JwtResult access = jwtProvider.issueAccessToken(
                user.getUserId().value(),
                user.getUsername().value(),
                authz.roles()
        );

        String refreshValue = jwtProvider.issueRefreshToken(user.getUserId().value());
        JwtProvider.Decoded refreshDecoded = jwtProvider.decodeAndVerify(refreshValue);
        if (refreshDecoded.tokenType() != JwtProvider.TokenType.REFRESH) {
            // Defensive: should never happen
            throw new AppException(ErrorCode.UNCATEGORIZED_EXCEPTION);
        }

        refreshTokenRepository.save(RefreshToken.issue(user.getUserId(), refreshValue, refreshDecoded.expiresAt()));

        return LoginResponse.builder()
                .accessToken(access.token())
                .refreshToken(refreshValue)
                .expiresAt(access.expiresAt())
                .build();
    }

    @Transactional
    public RegisterResponse register(RegisterRequest req) {
        // Optional confirmPassword check (application-level validation)
        if (req.getConfirmPassword() != null && !req.getConfirmPassword().isBlank()) {
            if (!Objects.equals(req.getPassword(), req.getConfirmPassword())) {
                throw new AppException(ErrorCode.INVALID_REQUEST, "Password confirmation does not match");
            }
        }

        Username username = Username.of(req.getUsername());
        if (userRepository.existsByUsername(username)) {
            throw new AppException(ErrorCode.USER_EXISTED);
        }

        Email email = Email.of(req.getEmail());
        if (userRepository.existsByEmail(email)) {
            throw new AppException(ErrorCode.USER_EMAIL_EXISTED);
        }

        PasswordHash hash = PasswordHash.of(passwordHasher.hash(req.getPassword()));
        User user = User.create(username, email, hash);

        // Application policy: self-registered user gets default ROLE_USER
        Role defaultRole = roleRepository.findByCode("ROLE_USER")
                .orElseThrow(() -> new AppException(ErrorCode.ROLE_NOT_EXISTED, "Default role ROLE_USER not existed"));

        user.assignRole(defaultRole.getRoleId());
        userRepository.save(user);

        // IMPORTANT: Không issue token ở đây theo yêu cầu
        return RegisterResponse.builder()
                .userId(user.getUserId().value())
                .username(user.getUsername().value())
                .email(user.getEmail().value())
                .build();
    }

    @Transactional
    public LoginResponse refresh(RefreshRequest req) {
        String refreshValue = req.getRefreshToken();

        RefreshToken stored = refreshTokenRepository.findByTokenValue(refreshValue)
                .orElseThrow(() -> new AppException(ErrorCode.REFRESH_TOKEN_INVALID));

        // Verify JWT signature & issuer + token type
        JwtProvider.Decoded decoded;
        try {
            decoded = jwtProvider.decodeAndVerify(refreshValue);
        } catch (Exception e) {
            throw new AppException(ErrorCode.REFRESH_TOKEN_INVALID);
        }

        if (decoded.tokenType() != JwtProvider.TokenType.REFRESH) {
            throw new AppException(ErrorCode.REFRESH_TOKEN_INVALID);
        }

        Instant now = clock.now();
        if (decoded.expiresAt() == null || decoded.expiresAt().isBefore(now)) {
            throw new AppException(ErrorCode.REFRESH_TOKEN_EXPIRED);
        }

        // Stored token state checks (server-side revoke + expiry)
        if (stored.isExpired(now)) throw new AppException(ErrorCode.REFRESH_TOKEN_EXPIRED);
        if (stored.isRevoked()) throw new AppException(ErrorCode.REFRESH_TOKEN_REVOKED);

        // Strong binding: refresh token must belong to the same userId
        if (decoded.userId() == null || !decoded.userId().equals(stored.getUserId().value())) {
            throw new AppException(ErrorCode.REFRESH_TOKEN_INVALID);
        }

        User user = userRepository.findById(stored.getUserId())
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_EXISTED));

        if (!user.canLogin()) throw new AppException(ErrorCode.USER_INACTIVE);

        Authorities authz = loadAuthorities(user);
        JwtProvider.JwtResult access = jwtProvider.issueAccessToken(
                user.getUserId().value(),
                user.getUsername().value(),
                authz.roles()
        );

        // Rotate refresh token
        stored.revoke();
        refreshTokenRepository.save(stored);

        String newRefresh = jwtProvider.issueRefreshToken(user.getUserId().value());
        JwtProvider.Decoded newRefreshDecoded = jwtProvider.decodeAndVerify(newRefresh);
        refreshTokenRepository.save(RefreshToken.issue(user.getUserId(), newRefresh, newRefreshDecoded.expiresAt()));

        return LoginResponse.builder()
                .accessToken(access.token())
                .refreshToken(newRefresh)
                .expiresAt(access.expiresAt())
                .build();
    }

    @Transactional
    public void logout(LogoutRequest req) {
        JwtProvider.Decoded decoded;
        try {
            decoded = jwtProvider.decodeAndVerify(req.getAccessToken());
        } catch (Exception e) {
            throw new AppException(ErrorCode.TOKEN_INVALID);
        }

        // Only access tokens are accepted here.
        if (decoded.tokenType() != JwtProvider.TokenType.ACCESS) {
            throw new AppException(ErrorCode.TOKEN_INVALID);
        }

        if (decoded.jti() != null) {
            revocationStore.revoke(decoded.jti(), decoded.expiresAt());
        }

        if (req.getRefreshToken() != null && !req.getRefreshToken().isBlank()) {
            refreshTokenRepository.findByTokenValue(req.getRefreshToken())
                    .ifPresent(rt -> {
                        rt.revoke();
                        refreshTokenRepository.save(rt);
                    });
        }
    }

    @Transactional(readOnly = true)
    public IntrospectResponse introspect(IntrospectRequest req) {
        try {
            JwtProvider.Decoded decoded = jwtProvider.decodeAndVerify(req.getToken());

            // Only access tokens are introspectable.
            if (decoded.tokenType() != JwtProvider.TokenType.ACCESS) {
                return IntrospectResponse.builder().valid(false).build();
            }

            boolean valid = decoded.expiresAt() != null
                    && decoded.expiresAt().isAfter(clock.now())
                    && (decoded.jti() == null || !revocationStore.isRevoked(decoded.jti()));

            return IntrospectResponse.builder().valid(valid).build();
        } catch (Exception e) {
            return IntrospectResponse.builder().valid(false).build();
        }
    }

    private Authorities loadAuthorities(User user) {
        var roles = roleRepository.findAllById(user.getRoleIds());
        Set<String> roleCodes = roles.stream().map(Role::getCode).collect(Collectors.toSet());

        Set<PermissionId> permIds = roles.stream()
                .flatMap(r -> r.getPermissionIds().stream())
                .collect(Collectors.toSet());

        var perms = permissionRepository.findAllById(permIds);
        Set<String> permCodes = perms.stream().map(Permission::getCode).collect(Collectors.toSet());

        return new Authorities(roleCodes, permCodes);
    }

    private record Authorities(Set<String> roles, Set<String> permissions) {
    }
}
