package com.dang.identityservice.application.dto.auth;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class IntrospectRequest {
    @NotBlank
    private String token;
}
