package com.dang.productservice.domain.model.entities;

import com.dang.productservice.domain.model.aggregates.Product;
import com.dang.productservice.domain.model.valueobjects.Money;
import jakarta.persistence.*;
import lombok.Getter;

import java.util.Objects;

@Entity
@Table(name = "product_variants")
@Getter
public class ProductVariant {
    @Id
    private String variantId;

    private String sku;
    private String size;
    private String color;
    private String material;

    @Embedded
    @AttributeOverride(name = "amount", column = @Column(name = "price_amount"))
    @AttributeOverride(name = "currency", column = @Column(name = "price_currency"))
    private Money price;

    private Integer stockQuantity;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id")
    private Product product;

    protected ProductVariant() {
        // for JPA
    }

    // ✅ CONSTRUCTOR CHÍNH - không cần variantId (tự generate)
    public ProductVariant(String sku, String size, String color, String material,
                          Money price, Integer stockQuantity) {
        validateInput(sku, price, stockQuantity);

        this.variantId = generateVariantId();
        this.sku = sku.trim();
        this.size = (size != null) ? size.trim() : null;
        this.color = (color != null) ? color.trim() : null;
        this.material = (material != null) ? material.trim() : null;
        this.price = price;
        this.stockQuantity = stockQuantity;
    }

    // ✅ CONSTRUCTOR với product (cho bidirectional relationship)
    public ProductVariant(String sku, String size, String color, String material,
                          Money price, Integer stockQuantity, Product product) {
        this(sku, size, color, material, price, stockQuantity);
        this.product = product;
    }

    // ✅ FACTORY METHOD
    public static ProductVariant create(String sku, Money price, Integer stockQuantity) {
        return new ProductVariant(sku, null, null, null, price, stockQuantity);
    }

    public static ProductVariant create(String sku, String size, String color,
                                        Money price, Integer stockQuantity) {
        return new ProductVariant(sku, size, color, null, price, stockQuantity);
    }

    public static ProductVariant create(String sku, String size, String color, String material,
                                        Money price, Integer stockQuantity) {
        return new ProductVariant(sku, size, color, material, price, stockQuantity);
    }

    private String generateVariantId() {
        return "var_" + java.util.UUID.randomUUID().toString().substring(0, 8);
    }

    private void validateInput(String sku, Money price, Integer stockQuantity) {
        if (sku == null || sku.trim().isEmpty()) {
            throw new IllegalArgumentException("SKU cannot be null or empty");
        }
        if (price == null ) {
            throw new IllegalArgumentException("Price cannot be null or negative");
        }
        if (stockQuantity == null || stockQuantity < 0) {
            throw new IllegalArgumentException("Stock quantity cannot be null or negative");
        }
    }

    // ✅ DOMAIN METHODS
    public void updatePrice(Money newPrice) {
        if (newPrice == null ) {
            throw new IllegalArgumentException("Price cannot be null or negative");
        }
        this.price = newPrice;
    }

    public void updateStock(Integer newQuantity) {
        if (newQuantity == null || newQuantity < 0) {
            throw new IllegalArgumentException("Stock quantity cannot be null or negative");
        }
        this.stockQuantity = newQuantity;
    }

    public void reduceStock(Integer quantity) {
        if (quantity == null || quantity <= 0) {
            throw new IllegalArgumentException("Quantity must be positive");
        }
        if (this.stockQuantity < quantity) {
            throw new IllegalStateException(
                    String.format("Insufficient stock. Available: %d, Requested: %d",
                            this.stockQuantity, quantity)
            );
        }
        this.stockQuantity -= quantity;
    }

    public void increaseStock(Integer quantity) {
        if (quantity == null || quantity <= 0) {
            throw new IllegalArgumentException("Quantity must be positive");
        }
        this.stockQuantity += quantity;
    }

    public boolean isInStock() {
        return this.stockQuantity > 0;
    }

    public boolean hasLowStock() {
        return this.stockQuantity > 0 && this.stockQuantity <= 10; // Threshold for low stock
    }

    public boolean isOutOfStock() {
        return this.stockQuantity == 0;
    }

    public Money calculateTotalValue() {
        return this.price.multiply(this.stockQuantity);
    }

    // ✅ PRODUCT ASSOCIATION METHODS
    public void associateWithProduct(Product product) {
        if (product == null) {
            throw new IllegalArgumentException("Product cannot be null");
        }
        this.product = product;
    }

    public void dissociateFromProduct() {
        this.product = null;
    }

    public boolean isAssociatedWithProduct() {
        return this.product != null;
    }

    public boolean belongsToProduct(String productId) {
        return this.product != null &&
                this.product.getProductId() != null &&
                this.product.getProductId().getId().equals(productId);
    }

    // ✅ VALIDATION METHODS
    public boolean hasSameAttributes(ProductVariant other) {
        if (other == null) return false;

        return Objects.equals(this.size, other.size) &&
                Objects.equals(this.color, other.color) &&
                Objects.equals(this.material, other.material);
    }

    public boolean hasAttribute(String attributeName, String value) {
        if (attributeName == null || value == null) return false;

        switch (attributeName.toLowerCase()) {
            case "size": return Objects.equals(this.size, value);
            case "color": return Objects.equals(this.color, value);
            case "material": return Objects.equals(this.material, value);
            default: return false;
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ProductVariant that = (ProductVariant) o;
        return Objects.equals(variantId, that.variantId) ||
                Objects.equals(sku, that.sku); // Also consider SKU as unique identifier
    }

    @Override
    public int hashCode() {
        return Objects.hash(variantId, sku);
    }

    @Override
    public String toString() {
        return "ProductVariant{" +
                "variantId='" + variantId + '\'' +
                ", sku='" + sku + '\'' +
                ", size='" + size + '\'' +
                ", color='" + color + '\'' +
                ", material='" + material + '\'' +
                ", price=" + price +
                ", stockQuantity=" + stockQuantity +
                ", productId=" + (product != null ? product.getProductId().getId() : "null") +
                '}';
    }
}