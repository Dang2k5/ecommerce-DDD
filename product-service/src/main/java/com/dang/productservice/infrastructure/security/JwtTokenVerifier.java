package com.dang.productservice.infrastructure.security;

import java.time.Instant;
import java.util.Set;

public interface JwtTokenVerifier {

    Decoded verify(String token);

    record Decoded(
            String userId,
            String username,
            String jti,
            Instant expiresAt,
            Set<String> roles,
            String type
    ) {}
}
