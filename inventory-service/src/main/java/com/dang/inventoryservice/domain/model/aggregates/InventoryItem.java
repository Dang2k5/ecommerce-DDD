package com.dang.inventoryservice.domain.model.aggregates;

import com.dang.inventoryservice.domain.model.valueobjects.Sku;
import jakarta.persistence.*;
import lombok.Getter;

@Entity
@Table(name = "inventory_items")
@Getter
public class InventoryItem {

    @EmbeddedId
    private Sku sku;

    @Column(name = "available_qty", nullable = false)
    private int availableQty;

    @Version
    private long version;

    protected InventoryItem() {
    }

    private InventoryItem(Sku sku, int availableQty) {
        if (availableQty < 0) throw new IllegalArgumentException("availableQty must be >= 0");
        this.sku = sku;
        this.availableQty = availableQty;
    }

    public static InventoryItem create(String sku, int availableQty) {
        return new InventoryItem(Sku.of(sku), availableQty);
    }

    public void stockIn(int qty) {
        if (qty <= 0) throw new IllegalArgumentException("qty must be > 0");
        this.availableQty += qty;
    }

    public void stockOut(int qty) {
        if (qty <= 0) throw new IllegalArgumentException("qty must be > 0");
        if (this.availableQty < qty) throw new IllegalStateException("Not enough stock for sku=" + sku.value());
        this.availableQty -= qty;
    }
}
