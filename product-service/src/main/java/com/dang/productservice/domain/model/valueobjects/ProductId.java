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
public class ProductId implements Serializable {

    private static final int UUID_LENGTH = 36;

    @Column(name = "product_id", nullable = false, length = UUID_LENGTH, updatable = false)
    private String value;

    protected ProductId() {
        // for JPA
    }

    private ProductId(String raw) {
        this.value = normalizeAndValidate(raw);
    }

    public static ProductId generate() {
        return new ProductId(UUID.randomUUID().toString());
    }

    public static ProductId of(String raw) {
        return new ProductId(raw);
    }

    public String value() {
        return value;
    }

    private static String normalizeAndValidate(String raw) {
        if (raw == null) throw new IllegalArgumentException("Product ID cannot be null");

        String normalized = raw.strip();
        if (normalized.isEmpty()) throw new IllegalArgumentException("Product ID cannot be empty");

        // Nếu bạn muốn ProductId luôn là UUID:
        try {
            UUID.fromString(normalized);
        } catch (IllegalArgumentException ex) {
            throw new IllegalArgumentException("Product ID must be a valid UUID: " + normalized);
        }

        return normalized;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof ProductId other)) return false;
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
