package com.dang.productservice.domain.model.valueobjects;

import jakarta.persistence.Access;
import jakarta.persistence.AccessType;
import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;

import java.io.Serializable;
import java.util.Objects;
import java.util.UUID;

@Embeddable
@Access(AccessType.FIELD)
public class CategoryId implements Serializable {

    private static final int UUID_LENGTH = 36;

    @Column(name = "category_id", nullable = false, length = UUID_LENGTH, updatable = false)
    private String value;

    protected CategoryId() {
        // for JPA
    }

    private CategoryId(String raw) {
        this.value = normalizeAndValidate(raw);
    }

    public static CategoryId generate() {
        return new CategoryId(UUID.randomUUID().toString());
    }

    public static CategoryId of(String raw) {
        return new CategoryId(raw);
    }

    public String value() {
        return value;
    }

    private static String normalizeAndValidate(String raw) {
        if (raw == null) {
            throw new IllegalArgumentException("Category ID cannot be null");
        }

        String normalized = raw.strip();
        if (normalized.isEmpty()) {
            throw new IllegalArgumentException("Category ID cannot be empty");
        }

        // Nếu CategoryId luôn là UUID thì validate luôn để tránh dữ liệu rác
        try {
            UUID.fromString(normalized);
        } catch (IllegalArgumentException ex) {
            throw new IllegalArgumentException("Category ID must be a valid UUID: " + normalized);
        }

        return normalized;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof CategoryId other)) return false;
        return Objects.equals(value, other.value);
    }

    @Override
    public int hashCode() {
        return Objects.hash(value);
    }

    @Override
    public String toString() {
        return value;
    }
}
