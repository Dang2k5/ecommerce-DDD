package com.dang.identityservice.application.dto.auth;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class LogoutRequest {
    @NotBlank
    private String accessToken;
    private String refreshToken; // optional
}
