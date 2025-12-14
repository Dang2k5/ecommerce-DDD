package com.dang.identityservice.application.dto.admin;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class AssignRoleRequest {
    @NotBlank
    private String roleCode; // ROLE_ADMIN
}
