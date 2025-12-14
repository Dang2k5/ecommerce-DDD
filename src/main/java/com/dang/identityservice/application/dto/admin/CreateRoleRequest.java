package com.dang.identityservice.application.dto.admin;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class CreateRoleRequest {
    @NotBlank
    private String code; // ROLE_ADMIN
    @NotBlank
    private String name;
}
