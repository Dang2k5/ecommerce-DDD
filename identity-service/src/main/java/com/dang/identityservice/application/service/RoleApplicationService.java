package com.dang.identityservice.application.service;

import com.dang.identityservice.application.dto.admin.AssignPermissionRequest;
import com.dang.identityservice.application.dto.admin.CreateRoleRequest;
import com.dang.identityservice.application.dto.admin.RoleResponse;
import com.dang.identityservice.application.dto.admin.UpdateRoleRequest;
import com.dang.identityservice.application.exception.AppException;
import com.dang.identityservice.application.exception.ErrorCode;
import com.dang.identityservice.domain.model.aggregates.Permission;
import com.dang.identityservice.domain.model.aggregates.Role;
import com.dang.identityservice.domain.model.valueobjects.PermissionId;
import com.dang.identityservice.domain.repository.PermissionRepository;
import com.dang.identityservice.domain.repository.RoleRepository;
import com.dang.identityservice.domain.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class RoleApplicationService {

    private final RoleRepository roleRepository;
    private final PermissionRepository permissionRepository;
    private final UserRepository userRepository;

    @Transactional
    public RoleResponse create(CreateRoleRequest req) {
        String code = req.getCode().strip().toUpperCase();
        if (roleRepository.existsByCode(code)) throw new AppException(ErrorCode.ROLE_EXISTED);

        final Role role;
        try {
            role = Role.create(code, req.getName());
        } catch (IllegalArgumentException e) {
            throw new AppException(ErrorCode.INVALID_REQUEST, e.getMessage());
        }
        roleRepository.save(role);

        return toResponse(role);
    }

    @Transactional(readOnly = true)
    public java.util.List<RoleResponse> list() {
        return roleRepository.findAll().stream().map(this::toResponse).toList();
    }

    @Transactional(readOnly = true)
    public RoleResponse getByCode(String code) {
        Role role = roleRepository.findByCode(code.strip().toUpperCase())
                .orElseThrow(() -> new AppException(ErrorCode.ROLE_NOT_EXISTED));
        return toResponse(role);
    }

    @Transactional
    public RoleResponse update(String code, UpdateRoleRequest req) {
        Role role = roleRepository.findByCode(code.strip().toUpperCase())
                .orElseThrow(() -> new AppException(ErrorCode.ROLE_NOT_EXISTED));

        if (req.getName() != null && !req.getName().isBlank()) {
            try {
                role.rename(req.getName());
            } catch (IllegalArgumentException e) {
                throw new AppException(ErrorCode.INVALID_REQUEST, e.getMessage());
            }
        }

        roleRepository.save(role);
        return toResponse(role);
    }

    @Transactional
    public void delete(String code) {
        Role role = roleRepository.findByCode(code.strip().toUpperCase())
                .orElseThrow(() -> new AppException(ErrorCode.ROLE_NOT_EXISTED));

        if (userRepository.existsByRoleId(role.getRoleId())) {
            throw new AppException(ErrorCode.ROLE_IN_USE);
        }

        roleRepository.delete(role);
    }

    @Transactional
    public RoleResponse addPermission(String roleCode, AssignPermissionRequest req) {
        Role role = roleRepository.findByCode(roleCode.strip().toUpperCase())
                .orElseThrow(() -> new AppException(ErrorCode.ROLE_NOT_EXISTED));

        Permission perm = permissionRepository.findByCode(req.getPermissionCode().strip().toUpperCase())
                .orElseThrow(() -> new AppException(ErrorCode.PERMISSION_NOT_EXISTED));

        role.assignPermission(perm.getPermissionId());
        roleRepository.save(role);

        return toResponse(role);
    }

    @Transactional
    public RoleResponse removePermission(String roleCode, AssignPermissionRequest req) {
        Role role = roleRepository.findByCode(roleCode.strip().toUpperCase())
                .orElseThrow(() -> new AppException(ErrorCode.ROLE_NOT_EXISTED));

        Permission perm = permissionRepository.findByCode(req.getPermissionCode().strip().toUpperCase())
                .orElseThrow(() -> new AppException(ErrorCode.PERMISSION_NOT_EXISTED));

        role.removePermission(perm.getPermissionId());
        roleRepository.save(role);

        return toResponse(role);
    }

    private RoleResponse toResponse(Role role) {
        Set<PermissionId> permIds = role.getPermissionIds();
        Set<String> perms = permissionRepository.findAllById(permIds).stream()
                .map(Permission::getCode)
                .collect(Collectors.toSet());

        return RoleResponse.builder()
                .roleId(role.getRoleId().value())
                .code(role.getCode())
                .name(role.getName())
                .permissions(perms)
                .build();
    }
}
