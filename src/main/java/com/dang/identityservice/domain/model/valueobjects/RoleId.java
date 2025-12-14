package com.dang.identityservice.domain.model.valueobjects;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;

import java.io.Serializable;
import java.util.Objects;
import java.util.UUID;

@Embeddable
public class RoleId implements Serializable {
    @Column(name = "role_id", nullable = false, length = 36, updatable = false)
    private String value;

    protected RoleId() {
    }

    private RoleId(String raw) {
        this.value = normalize(raw);
    }

    public static RoleId generate() {
        return new RoleId(UUID.randomUUID().toString());
    }

    public static RoleId of(String raw) {
        return new RoleId(raw);
    }

    public String value() {
        return value;
    }

    private static String normalize(String raw) {
        if (raw == null) throw new IllegalArgumentException("RoleId null");
        String v = raw.strip();
        if (v.isEmpty()) throw new IllegalArgumentException("RoleId empty");
        UUID.fromString(v);
        return v;
    }

    @Override
    public boolean equals(Object o) {
        return o instanceof RoleId other && Objects.equals(value, other.value);
    }

    @Override
    public int hashCode() {
        return Objects.hash(value);
    }
}
