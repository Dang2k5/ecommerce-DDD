package com.dang.productservice.domain.model.aggregates;

import com.dang.productservice.domain.model.entities.ProductVariant;
import com.dang.productservice.domain.model.valueobjects.*;
import jakarta.persistence.*;
import lombok.Getter;

import java.time.Instant;
import java.util.*;

@Entity
@Table(name = "products")
@Getter
public class Product {

    @EmbeddedId
    private ProductId productId;

    @Embedded
    private ProductDetails details;

    @Embedded
    @AttributeOverrides({
            @AttributeOverride(name = "amount", column = @Column(name = "base_price_amount", nullable = false)),
            @AttributeOverride(name = "currency", column = @Column(name = "base_price_currency", nullable = false, length = 3))
    })
    private Money basePrice;

    @Embedded
    @AttributeOverride(name = "value", column = @Column(name = "category_id", nullable = false, length = 36))
    private CategoryId categoryId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private ProductStatus status;

    @OneToMany(mappedBy = "product", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private final List<ProductVariant> variants = new ArrayList<>();

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    protected Product() {
        // for JPA
    }

    // ===== Factory =====
    public static Product create(ProductDetails details, Money basePrice, CategoryId categoryId) {
        Product product = new Product();
        product.productId = ProductId.generate();
        product.details = requireDetails(details);
        product.basePrice = requirePrice(basePrice);
        product.categoryId = requireCategoryId(categoryId);
        product.status = ProductStatus.ACTIVE;
        product.createdAt = Instant.now();
        product.updatedAt = product.createdAt;
        return product;
    }

    // ===== Domain methods =====
    public void updateDetails(ProductDetails newDetails) {
        this.details = requireDetails(newDetails);
        touch();
    }

    public void updateBasePrice(Money newPrice) {
        this.basePrice = requirePrice(newPrice);
        touch();
    }

    public void changeCategory(CategoryId newCategoryId) {
        this.categoryId = requireCategoryId(newCategoryId);
        touch();
    }

    public void activate() {
        if (this.status != ProductStatus.ACTIVE) {
            this.status = ProductStatus.ACTIVE;
            touch();
        }
    }

    public void deactivate() {
        if (this.status != ProductStatus.INACTIVE) {
            this.status = ProductStatus.INACTIVE;
            touch();
        }
    }

    public void markOutOfStock() {
        if (this.status != ProductStatus.OUT_OF_STOCK) {
            this.status = ProductStatus.OUT_OF_STOCK;
            touch();
        }
    }

    public void markInStock() {
        if (this.status == ProductStatus.OUT_OF_STOCK && isInStock()) {
            this.status = ProductStatus.ACTIVE;
            touch();
        }
    }

    // ===== Variant management =====
    public ProductVariant createVariant(String sku, Money price, int stockQuantity) {
        return createVariant(sku, null, null, null, price, stockQuantity);
    }

    public ProductVariant createVariant(String sku, String size, String color, Money price, int stockQuantity) {
        return createVariant(sku, size, color, null, price, stockQuantity);
    }

    public ProductVariant createVariant(String sku, String size, String color, String material, Money price, int stockQuantity) {
        String normalizedSku = requireSku(sku);

        if (findVariantBySku(normalizedSku).isPresent()) {
            throw new IllegalArgumentException("Variant with SKU " + normalizedSku + " already exists");
        }

        ProductVariant variant = ProductVariant.of(normalizedSku, size, color, material, price, stockQuantity);
        attachVariant(variant);

        touch();
        updateStockStatus();
        return variant;
    }

    public void addVariant(ProductVariant variant) {
        if (variant == null) throw new IllegalArgumentException("Variant cannot be null");

        String normalizedSku = requireSku(variant.getSku());
        if (findVariantBySku(normalizedSku).isPresent()) {
            throw new IllegalArgumentException("Variant with SKU " + normalizedSku + " already exists");
        }

        attachVariant(variant);
        touch();
        updateStockStatus();
    }

    public void removeVariant(String sku) {
        String normalizedSku = requireSku(sku);

        boolean removed = false;
        for (Iterator<ProductVariant> it = variants.iterator(); it.hasNext(); ) {
            ProductVariant v = it.next();
            if (normalizedSku.equals(v.getSku())) {
                v.detach();
                it.remove();
                removed = true;
            }
        }
        if (removed) {
            touch();
            updateStockStatus();
        }
    }

    public Optional<ProductVariant> findVariantBySku(String sku) {
        if (sku == null) return Optional.empty();
        String normalizedSku = sku.strip();
        if (normalizedSku.isEmpty()) return Optional.empty();

        return variants.stream()
                .filter(v -> normalizedSku.equals(v.getSku()))
                .findFirst();
    }

    public void updateVariantPrice(String sku, Money newPrice) {
        ProductVariant variant = findVariantBySku(sku)
                .orElseThrow(() -> new IllegalArgumentException("Variant with SKU " + sku + " not found"));

        variant.updatePrice(newPrice);
        touch();
    }

    public void updateVariantStock(String sku, int newStock) {
        ProductVariant variant = findVariantBySku(sku)
                .orElseThrow(() -> new IllegalArgumentException("Variant with SKU " + sku + " not found"));

        variant.updateStock(newStock);
        touch();
        updateStockStatus();
    }

    // ===== Business logic =====
    public boolean isInStock() {
        return variants.stream().anyMatch(ProductVariant::isInStock);
    }

    public Money getLowestPrice() {
        return variants.stream()
                .filter(ProductVariant::isInStock)
                .map(ProductVariant::getPrice)
                .min(Comparator.comparing(Money::amount))
                .orElse(basePrice);
    }

    public Money getHighestPrice() {
        return variants.stream()
                .filter(ProductVariant::isInStock)
                .map(ProductVariant::getPrice)
                .max(Comparator.comparing(Money::amount))
                .orElse(basePrice);
    }

    public int getTotalStock() {
        return variants.stream().mapToInt(ProductVariant::getStockQuantity).sum();
    }

    public boolean isActive() {
        return this.status == ProductStatus.ACTIVE;
    }

    public boolean hasVariants() {
        return !variants.isEmpty();
    }

    public int getVariantCount() {
        return variants.size();
    }

    public List<ProductVariant> getVariants() {
        return Collections.unmodifiableList(variants);
    }

    // ===== Helpers =====
    private void updateStockStatus() {
        if (this.status == ProductStatus.OUT_OF_STOCK && isInStock()) {
            markInStock();
        } else if (this.status == ProductStatus.ACTIVE && !isInStock()) {
            markOutOfStock();
        }
    }

    private void touch() {
        this.updatedAt = Instant.now();
    }

    private void attachVariant(ProductVariant variant) {
        variant.attachTo(this);
        variants.add(variant);
    }

    private static ProductDetails requireDetails(ProductDetails details) {
        if (details == null || details.isEmpty()) {
            throw new IllegalArgumentException("Product details cannot be null or empty");
        }
        return details;
    }

    private static Money requirePrice(Money price) {
        if (price == null || price.isNegative()) {
            throw new IllegalArgumentException("Price cannot be null or negative");
        }
        return price;
    }

    private static CategoryId requireCategoryId(CategoryId categoryId) {
        if (categoryId == null) {
            throw new IllegalArgumentException("Category ID cannot be null");
        }
        return categoryId;
    }

    private static String requireSku(String sku) {
        if (sku == null) throw new IllegalArgumentException("SKU cannot be null");
        String normalized = sku.strip();
        if (normalized.isEmpty()) throw new IllegalArgumentException("SKU cannot be empty");
        return normalized;
    }

    // ===== equals/hashCode =====
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Product other)) return false;
        return Objects.equals(productId, other.productId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(productId);
    }

    @Override
    public String toString() {
        return "Product{" +
                "productId=" + productId +
                ", name=" + (details != null ? details.name() : null) +
                ", status=" + status +
                ", categoryId=" + (categoryId != null ? categoryId.value() : null) +
                ", variantCount=" + variants.size() +
                '}';
    }
}
