package com.dang.identityservice.interfaces.rest;

import com.dang.identityservice.application.dto.ApiResponse;
import com.dang.identityservice.application.dto.admin.CreatePermissionRequest;
import com.dang.identityservice.application.dto.admin.PermissionResponse;
import com.dang.identityservice.application.dto.admin.UpdatePermissionRequest;
import com.dang.identityservice.application.service.PermissionApplicationService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin/permissions")
@RequiredArgsConstructor
public class PermissionController {

    private final PermissionApplicationService service;

    // New permission naming (preferred): PERM_PERMISSION_*
    // Backward compatible (legacy): PERMISSION_*
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    @PostMapping
    public ApiResponse<PermissionResponse> create(@Valid @RequestBody CreatePermissionRequest req) {
        return ApiResponse.ok(service.create(req));
    }

    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    @GetMapping
    public ApiResponse<java.util.List<PermissionResponse>> list() {
        return ApiResponse.ok(service.list());
    }

    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    @GetMapping("/{code}")
    public ApiResponse<PermissionResponse> get(@PathVariable String code) {
        return ApiResponse.ok(service.getByCode(code));
    }

    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    @PutMapping("/{code}")
    public ApiResponse<PermissionResponse> update(@PathVariable String code, @Valid @RequestBody UpdatePermissionRequest req) {
        return ApiResponse.ok(service.update(code, req));
    }

    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    @DeleteMapping("/{code}")
    public ApiResponse<Void> delete(@PathVariable String code) {
        service.delete(code);
        return ApiResponse.ok(null);
    }
}
