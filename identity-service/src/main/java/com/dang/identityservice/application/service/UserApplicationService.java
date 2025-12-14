package com.dang.identityservice.application.service;

import com.dang.identityservice.application.dto.admin.AssignRoleRequest;
import com.dang.identityservice.application.dto.admin.CreateUserRequest;
import com.dang.identityservice.application.dto.admin.UpdateUserRequest;
import com.dang.identityservice.application.dto.admin.UserResponse;
import com.dang.identityservice.application.exception.AppException;
import com.dang.identityservice.application.exception.ErrorCode;
import com.dang.identityservice.application.port.CurrentUserPort;
import com.dang.identityservice.application.port.PasswordHasher;
import com.dang.identityservice.domain.model.aggregates.Permission;
import com.dang.identityservice.domain.model.aggregates.Role;
import com.dang.identityservice.domain.model.aggregates.User;
import com.dang.identityservice.domain.model.valueobjects.*;
import com.dang.identityservice.domain.repository.PermissionRepository;
import com.dang.identityservice.domain.repository.RoleRepository;
import com.dang.identityservice.domain.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class UserApplicationService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PermissionRepository permissionRepository;
    private final PasswordHasher passwordHasher;
    private final CurrentUserPort currentUserPort;

    @Value("${bootstrap.admin.username:admin}")
    private String bootstrapAdminUsername;

    @Transactional
    public UserResponse create(CreateUserRequest req) {
        Username username = Username.of(req.getUsername());
        if (userRepository.existsByUsername(username)) throw new AppException(ErrorCode.USER_EXISTED);

        Email email = Email.of(req.getEmail());
        if (userRepository.existsByEmail(email)) throw new AppException(ErrorCode.USER_EMAIL_EXISTED);

        PasswordHash hash = PasswordHash.of(passwordHasher.hash(req.getPassword()));

        User user = User.create(username, email, hash);
        userRepository.save(user);

        return toResponse(user);
    }

    @Transactional(readOnly = true)
    public java.util.List<UserResponse> list() {
        return userRepository.findAll().stream().map(this::toResponse).toList();
    }

    @Transactional(readOnly = true)
    public UserResponse get(String userId) {
        User user = userRepository.findById(UserId.of(userId))
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_EXISTED));
        return toResponse(user);
    }

    @Transactional
    public UserResponse update(String userId, UpdateUserRequest req) {
        User user = userRepository.findById(UserId.of(userId))
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_EXISTED));

        if (req.getEmail() != null && !req.getEmail().isBlank()) {
            Email newEmail = Email.of(req.getEmail());
            if (!newEmail.equals(user.getEmail()) && userRepository.existsByEmail(newEmail)) {
                throw new AppException(ErrorCode.USER_EMAIL_EXISTED);
            }
            user.changeEmail(newEmail);
        }

        if (req.getPassword() != null && !req.getPassword().isBlank()) {
            user.changePassword(PasswordHash.of(passwordHasher.hash(req.getPassword())));
        }

        // Preferred: explicit status
        if (req.getStatus() != null && !req.getStatus().isBlank()) {
            try {
                user.changeStatus(UserStatus.valueOf(req.getStatus().strip().toUpperCase()));
            } catch (Exception e) {
                throw new AppException(ErrorCode.INVALID_REQUEST, "Invalid status. Allowed: ACTIVE, DISABLED, LOCKED, PENDING_ACTIVATION");
            }
        } else if (req.getActive() != null) {
            // Backward compatible flag
            if (req.getActive()) user.activate();
            else user.deactivate();
        }

        userRepository.save(user);
        return toResponse(user);
    }

    @Transactional
    public void delete(String userId) {
        User user = userRepository.findById(UserId.of(userId))
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_EXISTED));

        String currentUsername = currentUserPort.getUsername();
        if (currentUsername != null && currentUsername.equalsIgnoreCase(user.getUsername().value())) {
            throw new AppException(ErrorCode.CANNOT_DELETE_SELF);
        }

        if (user.getUsername().value().equalsIgnoreCase(bootstrapAdminUsername)) {
            throw new AppException(ErrorCode.CANNOT_DELETE_BOOTSTRAP_ADMIN);
        }

        userRepository.delete(user);
    }

    @Transactional
    public UserResponse assignRole(String userId, AssignRoleRequest req) {
        User user = userRepository.findById(UserId.of(userId))
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_EXISTED));

        Role role = roleRepository.findByCode(req.getRoleCode().strip().toUpperCase())
                .orElseThrow(() -> new AppException(ErrorCode.ROLE_NOT_EXISTED));

        user.assignRole(role.getRoleId());
        userRepository.save(user);

        return toResponse(user);
    }

    @Transactional
    public UserResponse removeRole(String userId, AssignRoleRequest req) {
        User user = userRepository.findById(UserId.of(userId))
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_EXISTED));

        Role role = roleRepository.findByCode(req.getRoleCode().strip().toUpperCase())
                .orElseThrow(() -> new AppException(ErrorCode.ROLE_NOT_EXISTED));

        user.removeRole(role.getRoleId());
        userRepository.save(user);

        return toResponse(user);
    }

    private UserResponse toResponse(User user) {
        var roles = roleRepository.findAllById(user.getRoleIds());

        Set<String> roleCodes = roles.stream()
                .map(Role::getCode)
                .collect(Collectors.toSet());

        Set<PermissionId> permIds = roles.stream()
                .flatMap(r -> r.getPermissionIds().stream())
                .collect(Collectors.toSet());

        Set<String> permCodes = permissionRepository.findAllById(permIds).stream()
                .map(Permission::getCode)
                .collect(Collectors.toSet());

        return UserResponse.builder()
                .userId(user.getUserId().value())
                .username(user.getUsername().value())
                .email(user.getEmail().value())
                .active(user.isActive())
                .status(user.getStatus() == null ? null : user.getStatus().name())
                .roles(roleCodes)
                .permissions(permCodes)
                .build();
    }
}
