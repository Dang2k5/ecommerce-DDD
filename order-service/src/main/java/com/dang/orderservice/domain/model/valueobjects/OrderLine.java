package com.dang.orderservice.domain.model.valueobjects;

import jakarta.persistence.*;

import java.math.BigDecimal;

@Embeddable
public class OrderLine {

    @Embedded
    private Sku sku;

    @Column(nullable = false)
    private int quantity;

    @Column(name = "unit_price", nullable = false, precision = 19, scale = 2)
    private BigDecimal unitPrice;

    protected OrderLine() {}

    public OrderLine(String sku, int quantity, BigDecimal unitPrice) {
        this(Sku.of(sku), quantity, unitPrice);
    }

    public OrderLine(Sku sku, int quantity, BigDecimal unitPrice) {
        if (sku == null) throw new IllegalArgumentException("sku is required");
        if (quantity <= 0) throw new IllegalArgumentException("quantity must be > 0");
        if (unitPrice == null) throw new IllegalArgumentException("unitPrice is required");

        this.sku = sku;
        this.quantity = quantity;
        this.unitPrice = unitPrice;
    }

    public String getSku() {
        return sku.value();
    }

    public int getQuantity() {
        return quantity;
    }

    public BigDecimal getUnitPrice() {
        return unitPrice;
    }
}
