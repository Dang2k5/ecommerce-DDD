package com.dang.inventoryservice.domain.model.valueobjects;

import com.fasterxml.jackson.annotation.JsonValue;
import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;

import java.io.Serializable;
import java.util.Objects;

@Embeddable
public class Sku implements Serializable {

    @Column(name = "sku", nullable = false, length = 64)
    private String value;

    protected Sku() {
    }

    private Sku(String value) {
        if (value == null || value.isBlank()) throw new IllegalArgumentException("SKU is required");
        this.value = value.strip();
    }

    public static Sku of(String value) {
        return new Sku(value);
    }

    @JsonValue
    public String value() {
        return value;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Sku other)) return false;
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
