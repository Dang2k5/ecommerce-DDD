package com.dang.identityservice.domain.model.valueobjects;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;

import java.io.Serializable;
import java.util.Objects;
import java.util.UUID;

@Embeddable
public class PermissionId implements Serializable {
    @Column(name = "permission_id", nullable = false, length = 36, updatable = false)
    private String value;

    protected PermissionId() {
    }

    private PermissionId(String raw) {
        this.value = normalize(raw);
    }

    public static PermissionId generate() {
        return new PermissionId(UUID.randomUUID().toString());
    }

    public static PermissionId of(String raw) {
        return new PermissionId(raw);
    }

    public String value() {
        return value;
    }

    private static String normalize(String raw) {
        if (raw == null) throw new IllegalArgumentException("PermissionId null");
        String v = raw.strip();
        if (v.isEmpty()) throw new IllegalArgumentException("PermissionId empty");
        UUID.fromString(v);
        return v;
    }

    @Override
    public boolean equals(Object o) {
        return o instanceof PermissionId other && Objects.equals(value, other.value);
    }

    @Override
    public int hashCode() {
        return Objects.hash(value);
    }
}
