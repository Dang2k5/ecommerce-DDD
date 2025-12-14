package com.dang.identityservice.infrastructure.redis;

import com.dang.identityservice.application.port.ClockPort;
import com.dang.identityservice.application.port.TokenRevocationStore;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.time.Instant;

@Component
public class RedisTokenRevocationStore implements TokenRevocationStore {

    private static final String PREFIX = "revoked:jti:";

    private final StringRedisTemplate redis;
    private final ClockPort clock;

    public RedisTokenRevocationStore(StringRedisTemplate redis, ClockPort clock) {
        this.redis = redis;
        this.clock = clock;
    }

    @Override
    public void revoke(String jti, Instant expiresAt) {
        if (jti == null || jti.isBlank()) return;
        if (expiresAt == null) return;

        long seconds = Duration.between(clock.now(), expiresAt).getSeconds();
        if (seconds <= 0) return;

        redis.opsForValue().set(PREFIX + jti, "1", Duration.ofSeconds(seconds));
    }

    @Override
    public boolean isRevoked(String jti) {
        if (jti == null || jti.isBlank()) return false;
        return Boolean.TRUE.equals(redis.hasKey(PREFIX + jti));
    }
}
