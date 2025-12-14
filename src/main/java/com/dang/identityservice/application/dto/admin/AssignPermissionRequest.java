package com.dang.identityservice.application.dto.admin;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class AssignPermissionRequest {
    @NotBlank
    private String permissionCode; // preferred: PERM_*, legacy accepted: USER_READ/ROLE_WRITE/PERMISSION_READ...
}
