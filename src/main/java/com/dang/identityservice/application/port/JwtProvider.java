package com.dang.identityservice.application.port;

import java.time.Instant;
import java.util.Set;

public interface JwtProvider {

    enum TokenType {
        ACCESS,
        REFRESH,
        UNKNOWN;

        public static TokenType fromClaim(String raw) {
            if (raw == null) return UNKNOWN;
            String v = raw.trim().toLowerCase();
            return switch (v) {
                case "access" -> ACCESS;
                case "refresh" -> REFRESH;
                default -> UNKNOWN;
            };
        }
    }

    record JwtResult(String token, Instant expiresAt, String jti) {}

    // ✅ ROLE-ONLY
    JwtResult issueAccessToken(String userId, String username, Set<String> roles);

    String issueRefreshToken(String userId);

    Decoded decodeAndVerify(String token);

    record Decoded(
            String userId,
            String username,
            String jti,
            Instant expiresAt,
            TokenType tokenType,
            Set<String> roles
    ) {}
}
