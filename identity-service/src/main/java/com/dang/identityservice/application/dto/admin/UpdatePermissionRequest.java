package com.dang.identityservice.application.dto.admin;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class UpdatePermissionRequest {
    @NotBlank
    private String name;
}