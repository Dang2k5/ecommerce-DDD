package com.dang.identityservice.domain.model.aggregates;

import com.dang.identityservice.domain.model.valueobjects.UserId;
import jakarta.persistence.*;
import lombok.Getter;

import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

@Entity
@Table(name = "refresh_tokens")
@Getter
public class RefreshToken {

    @Id
    @Column(name = "token_id", nullable = false, length = 36, updatable = false)
    private String tokenId;

    @Embedded
    private UserId userId;

    @Column(name = "token_value", nullable = false, unique = true, length = 700)
    private String tokenValue;

    @Column(name = "expires_at", nullable = false)
    private Instant expiresAt;

    @Column(name = "revoked", nullable = false)
    private boolean revoked;

    protected RefreshToken() {
    }

    private RefreshToken(String tokenId, UserId userId, String tokenValue, Instant expiresAt) {
        this.tokenId = Objects.requireNonNull(tokenId);
        this.userId = Objects.requireNonNull(userId);
        if (tokenValue == null || tokenValue.isBlank()) throw new IllegalArgumentException("refresh token required");
        this.tokenValue = tokenValue;
        this.expiresAt = Objects.requireNonNull(expiresAt);
        this.revoked = false;
    }

    public static RefreshToken issue(UserId userId, String tokenValue, Instant expiresAt) {
        return new RefreshToken(UUID.randomUUID().toString(), userId, tokenValue, expiresAt);
    }

    public boolean isExpired(Instant now) {
        return expiresAt.isBefore(Objects.requireNonNull(now));
    }

    public void revoke() {
        this.revoked = true;
    }
}
