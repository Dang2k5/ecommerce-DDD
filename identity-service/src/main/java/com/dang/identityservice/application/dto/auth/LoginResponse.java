package com.dang.identityservice.application.dto.auth;

import lombok.Builder;
import lombok.Data;

import java.time.Instant;

@Data
@Builder
public class LoginResponse {
    private String accessToken;
    private String refreshToken;
    private Instant expiresAt;
}
