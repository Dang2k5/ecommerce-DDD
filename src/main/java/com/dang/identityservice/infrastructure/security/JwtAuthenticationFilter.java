package com.dang.identityservice.infrastructure.security;

import com.dang.identityservice.application.dto.ApiResponse;
import com.dang.identityservice.application.exception.AppException;
import com.dang.identityservice.application.exception.ErrorCode;
import com.dang.identityservice.application.port.ClockPort;
import com.dang.identityservice.application.port.JwtProvider;
import com.dang.identityservice.application.port.TokenRevocationStore;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import tools.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.util.Set;
import java.util.stream.Collectors;

@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtProvider jwtProvider;
    private final TokenRevocationStore revocationStore;
    private final ClockPort clock;

    // giữ như bạn đang dùng (Jackson 3: tools.jackson...)
    private final ObjectMapper objectMapper = new ObjectMapper();

    public JwtAuthenticationFilter(JwtProvider jwtProvider, TokenRevocationStore revocationStore, ClockPort clock) {
        this.jwtProvider = jwtProvider;
        this.revocationStore = revocationStore;
        this.clock = clock;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws ServletException, IOException {

        String header = request.getHeader(HttpHeaders.AUTHORIZATION);
        if (header == null || !header.startsWith("Bearer ")) {
            chain.doFilter(request, response);
            return;
        }

        String token = header.substring("Bearer ".length()).trim();

        try {
            JwtProvider.Decoded decoded = jwtProvider.decodeAndVerify(token);

            // Only ACCESS tokens are accepted for Authorization header.
            if (decoded.tokenType() != JwtProvider.TokenType.ACCESS) {
                throw new AppException(ErrorCode.TOKEN_INVALID);
            }

            // Access tokens must carry principal username.
            if (decoded.username() == null || decoded.username().isBlank()) {
                throw new AppException(ErrorCode.TOKEN_INVALID);
            }

            if (decoded.expiresAt() == null || decoded.expiresAt().isBefore(clock.now())) {
                throw new AppException(ErrorCode.TOKEN_INVALID);
            }

            if (decoded.jti() != null && revocationStore.isRevoked(decoded.jti())) {
                throw new AppException(ErrorCode.TOKEN_REVOKED);
            }

            // ✅ ROLE-ONLY: chỉ lấy ROLE_*, bỏ mọi thứ khác
            Set<SimpleGrantedAuthority> authorities =
                    (decoded.roles() == null ? Set.<String>of() : decoded.roles())
                            .stream()
                            .filter(r -> r != null && !r.isBlank() && r.startsWith("ROLE_"))
                            .map(SimpleGrantedAuthority::new)
                            .collect(Collectors.toUnmodifiableSet());

            var auth = new UsernamePasswordAuthenticationToken(decoded.username(), null, authorities);
            SecurityContextHolder.getContext().setAuthentication(auth);

            chain.doFilter(request, response);
        } catch (AppException ex) {
            writeError(response, ex.getErrorCode());
        } catch (Exception ex) {
            writeError(response, ErrorCode.TOKEN_INVALID);
        }
    }

    private void writeError(HttpServletResponse response, ErrorCode code) throws IOException {
        SecurityContextHolder.clearContext();
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.getWriter().write(
                objectMapper.writeValueAsString(
                        ApiResponse.error(code.getCode(), code.getMessage())
                )
        );
    }
}
