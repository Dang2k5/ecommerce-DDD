package com.dang.identityservice.application.port;

import java.time.Instant;

public interface TokenRevocationStore {
    void revoke(String jti, Instant expiresAt);

    boolean isRevoked(String jti);
}
