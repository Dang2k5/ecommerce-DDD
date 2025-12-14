package com.dang.identityservice.interfaces.rest;

import com.dang.identityservice.application.dto.ApiResponse;
import com.dang.identityservice.application.dto.admin.AssignPermissionRequest;
import com.dang.identityservice.application.dto.admin.CreateRoleRequest;
import com.dang.identityservice.application.dto.admin.RoleResponse;
import com.dang.identityservice.application.dto.admin.UpdateRoleRequest;
import com.dang.identityservice.application.service.RoleApplicationService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin/roles")
@RequiredArgsConstructor
public class RoleController {

    private final RoleApplicationService service;

    // New permission naming (preferred): PERM_ROLE_*
    // Backward compatible (legacy): ROLE_READ/ROLE_WRITE (permission codes)
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    @PostMapping
    public ApiResponse<RoleResponse> create(@Valid @RequestBody CreateRoleRequest req) {
        return ApiResponse.ok(service.create(req));
    }

    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    @GetMapping
    public ApiResponse<java.util.List<RoleResponse>> list() {
        return ApiResponse.ok(service.list());
    }

    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    @GetMapping("/{code}")
    public ApiResponse<RoleResponse> get(@PathVariable String code) {
        return ApiResponse.ok(service.getByCode(code));
    }

    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    @PutMapping("/{code}")
    public ApiResponse<RoleResponse> update(@PathVariable String code, @Valid @RequestBody UpdateRoleRequest req) {
        return ApiResponse.ok(service.update(code, req));
    }

    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    @DeleteMapping("/{code}")
    public ApiResponse<Void> delete(@PathVariable String code) {
        service.delete(code);
        return ApiResponse.ok(null);
    }

    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    @PostMapping("/{roleCode}/permissions")
    public ApiResponse<RoleResponse> addPerm(@PathVariable String roleCode, @Valid @RequestBody AssignPermissionRequest req) {
        return ApiResponse.ok(service.addPermission(roleCode, req));
    }

    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    @DeleteMapping("/{roleCode}/permissions")
    public ApiResponse<RoleResponse> removePerm(@PathVariable String roleCode, @Valid @RequestBody AssignPermissionRequest req) {
        return ApiResponse.ok(service.removePermission(roleCode, req));
    }
}
