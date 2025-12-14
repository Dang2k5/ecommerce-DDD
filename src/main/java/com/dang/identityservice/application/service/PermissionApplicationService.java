package com.dang.identityservice.application.service;

import com.dang.identityservice.application.dto.admin.CreatePermissionRequest;
import com.dang.identityservice.application.dto.admin.PermissionResponse;
import com.dang.identityservice.application.dto.admin.UpdatePermissionRequest;
import com.dang.identityservice.application.exception.AppException;
import com.dang.identityservice.application.exception.ErrorCode;
import com.dang.identityservice.domain.model.aggregates.Permission;
import com.dang.identityservice.domain.repository.PermissionRepository;
import com.dang.identityservice.domain.repository.RoleRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class PermissionApplicationService {

    private final PermissionRepository permissionRepository;
    private final RoleRepository roleRepository;

    public PermissionApplicationService(PermissionRepository permissionRepository, RoleRepository roleRepository) {
        this.permissionRepository = permissionRepository;
        this.roleRepository = roleRepository;
    }

    @Transactional
    public PermissionResponse create(CreatePermissionRequest req) {
        String code = req.getCode().strip().toUpperCase();
        if (permissionRepository.existsByCode(code)) throw new AppException(ErrorCode.PERMISSION_EXISTED);

        final Permission p;
        try {
            p = Permission.create(code, req.getName());
        } catch (IllegalArgumentException e) {
            throw new AppException(ErrorCode.INVALID_REQUEST, e.getMessage());
        }
        permissionRepository.save(p);
        return toResponse(p);
    }

    @Transactional(readOnly = true)
    public java.util.List<PermissionResponse> list() {
        return permissionRepository.findAll().stream().map(this::toResponse).toList();
    }

    @Transactional(readOnly = true)
    public PermissionResponse getByCode(String code) {
        Permission p = permissionRepository.findByCode(code.strip().toUpperCase())
                .orElseThrow(() -> new AppException(ErrorCode.PERMISSION_NOT_EXISTED));
        return toResponse(p);
    }

    @Transactional
    public PermissionResponse update(String code, UpdatePermissionRequest req) {
        Permission p = permissionRepository.findByCode(code.strip().toUpperCase())
                .orElseThrow(() -> new AppException(ErrorCode.PERMISSION_NOT_EXISTED));

        // DDD: entity tự quản lý hành vi thay đổi state
        if (req.getName() != null && !req.getName().isBlank()) {
            try {
                p.rename(req.getName());
            } catch (IllegalArgumentException e) {
                throw new AppException(ErrorCode.INVALID_REQUEST, e.getMessage());
            }
        }

        permissionRepository.save(p);
        return toResponse(p);
    }

    @Transactional
    public void delete(String code) {
        Permission p = permissionRepository.findByCode(code.strip().toUpperCase())
                .orElseThrow(() -> new AppException(ErrorCode.PERMISSION_NOT_EXISTED));

        if (roleRepository.existsByPermissionId(p.getPermissionId())) {
            throw new AppException(ErrorCode.PERMISSION_IN_USE);
        }

        permissionRepository.delete(p);
    }

    private PermissionResponse toResponse(Permission p) {
        return PermissionResponse.builder()
                .permissionId(p.getPermissionId().value())
                .code(p.getCode())
                .name(p.getName())
                .build();
    }
}
