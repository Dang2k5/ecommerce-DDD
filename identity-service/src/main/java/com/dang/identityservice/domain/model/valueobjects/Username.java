package com.dang.identityservice.domain.model.valueobjects;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;

import java.io.Serializable;
import java.util.Objects;

@Embeddable
public class Username implements Serializable {
    @Column(name = "username", nullable = false, unique = true, length = 50)
    private String value;

    protected Username() {
    }

    private Username(String raw) {
        this.value = normalize(raw);
    }

    public static Username of(String raw) {
        return new Username(raw);
    }

    public String value() {
        return value;
    }

    private static String normalize(String raw) {
        if (raw == null) throw new IllegalArgumentException("Username null");
        String v = raw.strip();
        if (v.isEmpty()) throw new IllegalArgumentException("Username empty");
        if (v.length() < 3) throw new IllegalArgumentException("Username too short");
        if (v.length() > 50) throw new IllegalArgumentException("Username too long");
        return v;
    }

    @Override
    public boolean equals(Object o) {
        return o instanceof Username other && Objects.equals(value, other.value);
    }

    @Override
    public int hashCode() {
        return Objects.hash(value);
    }
}
