package com.dang.paymentservice.domain.model.valueobjects;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;

import java.io.Serializable;
import java.util.Objects;
import java.util.UUID;

@Embeddable
public class OrderId implements Serializable {

    @Column(name = "order_id", nullable = false, updatable = false, length = 36)
    private String value;

    protected OrderId() {}

    private OrderId(String value) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException("OrderId is required");
        }
        this.value = value;
    }

    public static OrderId of(String value) {
        return new OrderId(value);
    }

    public static OrderId generate() {
        return new OrderId(UUID.randomUUID().toString());
    }

    public String value() {
        return value;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof OrderId other)) return false;
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