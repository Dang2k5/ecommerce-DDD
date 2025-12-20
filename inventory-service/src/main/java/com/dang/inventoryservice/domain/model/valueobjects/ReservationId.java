package com.dang.inventoryservice.domain.model.valueobjects;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;

import java.io.Serializable;
import java.util.Objects;

@Embeddable
public class ReservationId implements Serializable {

    @Column(name = "reservation_id", nullable = false, length = 64)
    private String value;

    protected ReservationId() {
    }

    private ReservationId(String value) {
        if (value == null || value.isBlank()) throw new IllegalArgumentException("ReservationId is required");
        this.value = value.strip();
    }

    public static ReservationId of(String value) {
        return new ReservationId(value);
    }

    public String value() {
        return value;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof ReservationId other)) return false;
        return Objects.equals(value, other.value);
    }

    @Override
    public int hashCode() {
        return Objects.hash(value);
    }
}
