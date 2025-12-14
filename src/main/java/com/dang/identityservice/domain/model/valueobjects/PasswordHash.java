package com.dang.identityservice.domain.model.valueobjects;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;

import java.io.Serializable;
import java.util.Objects;

@Embeddable
public class PasswordHash implements Serializable {
    @Column(name = "password_hash", nullable = false, length = 255)
    private String value;

    protected PasswordHash() {
    }

    private PasswordHash(String hashed) {
        if (hashed == null || hashed.isBlank()) throw new IllegalArgumentException("PasswordHash blank");
        this.value = hashed;
    }

    public static PasswordHash of(String hashed) {
        return new PasswordHash(hashed);
    }

    public String value() {
        return value;
    }

    @Override
    public boolean equals(Object o) {
        return o instanceof PasswordHash other && Objects.equals(value, other.value);
    }

    @Override
    public int hashCode() {
        return Objects.hash(value);
    }
}
