package com.dang.paymentservice.domain.model.valueobjects;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;

import java.io.Serializable;
import java.util.Objects;
import java.util.UUID;

@Embeddable
public class PaymentId implements Serializable {

    @Column(name = "payment_id", nullable = false, updatable = false, length = 36)
    private String value;

    protected PaymentId() {}

    private PaymentId(String value) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException("PaymentId is required");
        }
        this.value = value;
    }

    public static PaymentId of(String value) {
        return new PaymentId(value);
    }

    public static PaymentId generate() {
        return new PaymentId(UUID.randomUUID().toString());
    }

    public String value() {
        return value;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof PaymentId other)) return false;
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