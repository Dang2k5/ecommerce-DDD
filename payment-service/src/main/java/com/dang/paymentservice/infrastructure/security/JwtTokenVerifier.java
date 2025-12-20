package com.dang.paymentservice.infrastructure.security;

import java.time.Instant;
import java.util.Set;

public interface JwtTokenVerifier {

    record Decoded(
            String userId,
            String username,
            String jti,
            Instant expiresAt,
            Set<String> roles,
            String type
    ) {}

    Decoded verify(String token);
}
