package com.dang.orderservice.domain.model.valueobjects;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;

import java.io.Serializable;
import java.util.Objects;

@Embeddable
public class CustomerId implements Serializable {

    @Column(name = "customer_id", nullable = false, length = 64)
    private String value;

    protected CustomerId() {}

    private CustomerId(String value) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException("CustomerId is required");
        }
        this.value = value.strip();
    }

    public static CustomerId of(String value) {
        return new CustomerId(value);
    }

    public String value() {
        return value;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof CustomerId other)) return false;
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
