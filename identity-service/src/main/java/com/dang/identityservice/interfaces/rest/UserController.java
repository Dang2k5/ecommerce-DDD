package com.dang.identityservice.interfaces.rest;

import com.dang.identityservice.application.dto.ApiResponse;
import com.dang.identityservice.application.dto.admin.AssignRoleRequest;
import com.dang.identityservice.application.dto.admin.CreateUserRequest;
import com.dang.identityservice.application.dto.admin.UpdateUserRequest;
import com.dang.identityservice.application.dto.admin.UserResponse;
import com.dang.identityservice.application.service.UserApplicationService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin/users")
@RequiredArgsConstructor
public class UserController {

    private final UserApplicationService service;

    // New permission naming (preferred): PERM_USER_*
    // Backward compatible (legacy): USER_*
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    @PostMapping
    public ApiResponse<UserResponse> create(@Valid @RequestBody CreateUserRequest req) {
        return ApiResponse.ok(service.create(req));
    }

    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    @GetMapping
    public ApiResponse<java.util.List<UserResponse>> list() {
        return ApiResponse.ok(service.list());
    }

    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    @GetMapping("/{userId}")
    public ApiResponse<UserResponse> get(@PathVariable String userId) {
        return ApiResponse.ok(service.get(userId));
    }

    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    @PutMapping("/{userId}")
    public ApiResponse<UserResponse> update(@PathVariable String userId, @Valid @RequestBody UpdateUserRequest req) {
        return ApiResponse.ok(service.update(userId, req));
    }

    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    @DeleteMapping("/{userId}")
    public ApiResponse<Void> delete(@PathVariable String userId) {
        service.delete(userId);
        return ApiResponse.ok(null);
    }

    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    @PostMapping("/{userId}/roles")
    public ApiResponse<UserResponse> assignRole(@PathVariable String userId, @Valid @RequestBody AssignRoleRequest req) {
        return ApiResponse.ok(service.assignRole(userId, req));
    }

    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    @DeleteMapping("/{userId}/roles")
    public ApiResponse<UserResponse> removeRole(@PathVariable String userId, @Valid @RequestBody AssignRoleRequest req) {
        return ApiResponse.ok(service.removeRole(userId, req));
    }
}
