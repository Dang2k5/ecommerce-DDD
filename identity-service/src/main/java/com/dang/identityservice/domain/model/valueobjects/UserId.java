package com.dang.identityservice.domain.model.valueobjects;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;

import java.io.Serializable;
import java.util.Objects;
import java.util.UUID;

@Embeddable
public class UserId implements Serializable {
    @Column(name = "user_id", nullable = false, length = 36, updatable = false)
    private String value;

    protected UserId() {
    }

    private UserId(String raw) {
        this.value = normalize(raw);
    }

    public static UserId generate() {
        return new UserId(UUID.randomUUID().toString());
    }

    public static UserId of(String raw) {
        return new UserId(raw);
    }

    public String value() {
        return value;
    }

    private static String normalize(String raw) {
        if (raw == null) throw new IllegalArgumentException("UserId null");
        String v = raw.strip();
        if (v.isEmpty()) throw new IllegalArgumentException("UserId empty");
        UUID.fromString(v);
        return v;
    }

    @Override
    public boolean equals(Object o) {
        return o instanceof UserId other && Objects.equals(value, other.value);
    }

    @Override
    public int hashCode() {
        return Objects.hash(value);
    }
}
