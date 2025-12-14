package com.dang.productservice.domain.model.entities;

import com.dang.productservice.domain.model.aggregates.Product;
import com.dang.productservice.domain.model.valueobjects.Money;
import jakarta.persistence.*;
import lombok.Getter;

import java.io.Serializable;
import java.util.Objects;
import java.util.UUID;

@Entity
@Table(
        name = "product_variants",
        uniqueConstraints = {
                @UniqueConstraint(name = "uk_product_variants_sku", columnNames = "sku")
        },
        indexes = {
                @Index(name = "idx_product_variants_product_id", columnList = "product_id")
        }
)
@Getter
public class ProductVariant implements Serializable {

    @Id
    @Column(name = "variant_id", nullable = false, length = 50, updatable = false)
    private String variantId;

    @Column(name = "sku", nullable = false, length = 100)
    private String sku;

    @Column(name = "size", length = 50)
    private String size;

    @Column(name = "color", length = 50)
    private String color;

    @Column(name = "material", length = 50)
    private String material;

    @Embedded
    @AttributeOverrides({
            @AttributeOverride(name = "amount", column = @Column(name = "price_amount", nullable = false)),
            @AttributeOverride(name = "currency", column = @Column(name = "price_currency", nullable = false, length = 3))
    })
    private Money price;

    @Column(name = "stock_quantity", nullable = false)
    private int stockQuantity;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;

    protected ProductVariant() {
        // for JPA
    }

    private ProductVariant(String sku,
                           String size,
                           String color,
                           String material,
                           Money price,
                           int stockQuantity) {
        this.variantId = newVariantId();
        this.sku = normalizeAndValidateSku(sku);
        this.size = normalizeNullable(size, 50);
        this.color = normalizeNullable(color, 50);
        this.material = normalizeNullable(material, 50);
        this.price = requirePrice(price);
        this.stockQuantity = validateStock(stockQuantity);
    }

    // ===== Factory =====
    public static ProductVariant of(String sku,
                                    String size,
                                    String color,
                                    String material,
                                    Money price,
                                    int stockQuantity) {
        return new ProductVariant(sku, size, color, material, price, stockQuantity);
    }

    public static ProductVariant simple(String sku, Money price, int stockQuantity) {
        return new ProductVariant(sku, null, null, null, price, stockQuantity);
    }

    // ===== Domain methods =====
    public void updatePrice(Money newPrice) {
        this.price = requirePrice(newPrice);
    }

    public void updateStock(int newQuantity) {
        this.stockQuantity = validateStock(newQuantity);
    }

    public void reduceStock(int quantity) {
        if (quantity <= 0) throw new IllegalArgumentException("Quantity must be positive");
        if (stockQuantity < quantity) {
            throw new IllegalStateException("Insufficient stock. Available: " + stockQuantity + ", Requested: " + quantity);
        }
        stockQuantity -= quantity;
    }

    public void increaseStock(int quantity) {
        if (quantity <= 0) throw new IllegalArgumentException("Quantity must be positive");
        stockQuantity += quantity;
    }

    public boolean isInStock() {
        return stockQuantity > 0;
    }

    public boolean isOutOfStock() {
        return stockQuantity == 0;
    }

    public boolean hasLowStock(int threshold) {
        if (threshold <= 0) throw new IllegalArgumentException("Threshold must be positive");
        return stockQuantity > 0 && stockQuantity <= threshold;
    }

    public Money totalValue() {
        return price.multiply(stockQuantity);
    }

    // ===== Association (đơn giản, thường aggregate root Product sẽ quản) =====

    /**
     * Aggregate root (Product) sẽ gọi để đảm bảo quan hệ 2 chiều nhất quán.
     * Public để tránh hạn chế package khi tách aggregates/entities.
     */
    public void attachTo(Product product) {
        this.product = Objects.requireNonNull(product, "Product cannot be null");
    }

    public void detach() {
        this.product = null;
    }

    // ===== Helpers =====
    private static String newVariantId() {
        return "var_" + UUID.randomUUID().toString().replace("-", "").substring(0, 12);
    }

    private static String normalizeAndValidateSku(String raw) {
        if (raw == null) throw new IllegalArgumentException("SKU cannot be null");
        String v = raw.strip();
        if (v.isEmpty()) throw new IllegalArgumentException("SKU cannot be empty");
        if (v.length() > 100) throw new IllegalArgumentException("SKU cannot exceed 100 characters");
        return v;
    }

    private static String normalizeNullable(String raw, int maxLen) {
        if (raw == null) return null;
        String v = raw.strip();
        if (v.isEmpty()) return null;
        if (v.length() > maxLen) throw new IllegalArgumentException("Text cannot exceed " + maxLen + " characters");
        return v;
    }

    private static Money requirePrice(Money price) {
        if (price == null) throw new IllegalArgumentException("Price cannot be null");
        if (price.isNegative()) throw new IllegalArgumentException("Price cannot be negative");
        return price;
    }

    private static int validateStock(int qty) {
        if (qty < 0) throw new IllegalArgumentException("Stock quantity cannot be negative");
        return qty;
    }

    // ===== equals/hashCode theo PK =====
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof ProductVariant other)) return false;
        return Objects.equals(variantId, other.variantId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(variantId);
    }

    @Override
    public String toString() {
        return "ProductVariant{" +
                "variantId='" + variantId + '\'' +
                ", sku='" + sku + '\'' +
                ", stockQuantity=" + stockQuantity +
                '}';
    }
}
