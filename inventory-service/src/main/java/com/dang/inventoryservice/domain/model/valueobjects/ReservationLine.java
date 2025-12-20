package com.dang.inventoryservice.domain.model.valueobjects;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import jakarta.persistence.Embedded;

import java.io.Serializable;
import java.util.Objects;

@Embeddable
public class ReservationLine implements Serializable {

    @Embedded
    private Sku sku;

    @Column(nullable = false)
    private int quantity;

    protected ReservationLine() {
    }

    private ReservationLine(Sku sku, int quantity) {
        if (sku == null) throw new IllegalArgumentException("SKU is required");
        if (quantity <= 0) throw new IllegalArgumentException("Quantity must be > 0");
        this.sku = sku;
        this.quantity = quantity;
    }

    public static ReservationLine of(String sku, int quantity) {
        return new ReservationLine(Sku.of(sku), quantity);
    }

    public String getSku() {
        return sku.value();
    }

    public int getQuantity() {
        return quantity;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof ReservationLine other)) return false;
        return quantity == other.quantity && Objects.equals(sku, other.sku);
    }

    @Override
    public int hashCode() {
        return Objects.hash(sku, quantity);
    }
}
