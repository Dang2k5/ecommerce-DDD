package com.dang.identityservice.application.dto.admin;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

@Data
public class CreatePermissionRequest {
    @NotBlank
    @Pattern(
            regexp = "^(?!ROLE_)[A-Za-z0-9_]+$",
            message = "Permission code must not start with ROLE_. Use PERM_* (e.g. PERM_USER_READ)"
    )
    private String code; // e.g. PERM_USER_READ

    @NotBlank
    private String name;
}
