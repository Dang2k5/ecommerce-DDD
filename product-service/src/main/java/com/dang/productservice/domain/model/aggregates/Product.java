package com.dang.productservice.domain.model.aggregates;

import com.dang.productservice.domain.model.entities.ProductVariant;
import com.dang.productservice.domain.model.entities.Review;
import com.dang.productservice.domain.model.valueobjects.*;
import jakarta.persistence.*;
import lombok.Getter;

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
    @AttributeOverride(name = "amount", column = @Column(name = "base_price_amount"))
    @AttributeOverride(name = "currency", column = @Column(name = "base_price_currency"))
    private Money basePrice;

    private String categoryId;

    @Enumerated(EnumType.STRING)
    private ProductStatus status;

    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id")
    private List<ProductVariant> variants = new ArrayList<>();

    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id")
    private List<Review> reviews = new ArrayList<>();

    @Embedded
    private ProductStatistics statistics;

    @Column(name = "created_at")
    private Long createdAt;

    @Column(name = "updated_at")
    private Long updatedAt;

    protected Product() {
        // for JPA
    }

    // Factory method for creating new product
    public static Product create(ProductDetails details, Money basePrice,
                                 String categoryId ) {
        Product product = new Product();
        product.productId = ProductId.generate();
        product.details = details;
        product.basePrice = basePrice;
        product.categoryId = categoryId;
        product.status = ProductStatus.ACTIVE;
        product.statistics = new ProductStatistics();
        product.createdAt = System.currentTimeMillis();
        product.updatedAt = System.currentTimeMillis();

        return product;
    }

    // Domain methods
    public void updateDetails(ProductDetails newDetails) {
        if (newDetails == null || newDetails.isEmpty()) {
            throw new IllegalArgumentException("Product details cannot be null or empty");
        }
        this.details = newDetails;
        this.updatedAt = System.currentTimeMillis();
    }

    public void updateBasePrice(Money newPrice) {
        if (newPrice == null || newPrice.isNegative()) {
            throw new IllegalArgumentException("Price cannot be null or negative");
        }
        this.basePrice = newPrice;
        this.updatedAt = System.currentTimeMillis();
    }

    public void changeCategory(String newCategoryId) {
        if (newCategoryId == null || newCategoryId.isBlank()) {
            throw new IllegalArgumentException("Category ID cannot be null or empty");
        }
        this.categoryId = newCategoryId;
        this.updatedAt = System.currentTimeMillis();
    }

    public void activate() {
        if (this.status != ProductStatus.ACTIVE) {
            this.status = ProductStatus.ACTIVE;
            this.updatedAt = System.currentTimeMillis();
        }
    }

    public void deactivate() {
        if (this.status != ProductStatus.INACTIVE) {
            this.status = ProductStatus.INACTIVE;
            this.updatedAt = System.currentTimeMillis();
        }
    }

    public void markOutOfStock() {
        if (this.status != ProductStatus.OUT_OF_STOCK) {
            this.status = ProductStatus.OUT_OF_STOCK;
            this.updatedAt = System.currentTimeMillis();
        }
    }

    public void markInStock() {
        if (this.status == ProductStatus.OUT_OF_STOCK && this.isInStock()) {
            this.status = ProductStatus.ACTIVE;
            this.updatedAt = System.currentTimeMillis();
        }
    }

    // Variant management - FIXED
    public ProductVariant createVariant(String sku, Money price, Integer stockQuantity) {
        return createVariant(sku, null, null, null, price, stockQuantity);
    }

    public ProductVariant createVariant(String sku, String size, String color,
                                        Money price, Integer stockQuantity) {
        return createVariant(sku, size, color, null, price, stockQuantity);
    }

    public ProductVariant createVariant(String sku, String size, String color, String material,
                                        Money price, Integer stockQuantity) {
        // Check for duplicate SKU
        if (findVariantBySku(sku).isPresent()) {
            throw new IllegalArgumentException("Variant with SKU " + sku + " already exists");
        }

        ProductVariant variant = new ProductVariant(sku, size, color, material, price, stockQuantity);
        variants.add(variant);
        this.updatedAt = System.currentTimeMillis();

        // Auto update stock status
        updateStockStatus();

        return variant;
    }

    public void addVariant(ProductVariant variant) {
        if (variant == null) {
            throw new IllegalArgumentException("Variant cannot be null");
        }

        // Check for duplicate SKU
        if (findVariantBySku(variant.getSku()).isPresent()) {
            throw new IllegalArgumentException("Variant with SKU " + variant.getSku() + " already exists");
        }

        variants.add(variant);
        this.updatedAt = System.currentTimeMillis();

        // Auto update stock status
        updateStockStatus();
    }

    public void removeVariant(String sku) {
        boolean removed = variants.removeIf(variant -> variant.getSku().equals(sku));
        if (removed) {
            this.updatedAt = System.currentTimeMillis();
            updateStockStatus();
        }
    }

    public Optional<ProductVariant> findVariantBySku(String sku) {
        return variants.stream()
                .filter(variant -> variant.getSku().equals(sku))
                .findFirst();
    }

    public void updateVariantPrice(String sku, Money newPrice) {
        ProductVariant variant = findVariantBySku(sku)
                .orElseThrow(() -> new IllegalArgumentException("Variant with SKU " + sku + " not found"));

        variant.updatePrice(newPrice);
        this.updatedAt = System.currentTimeMillis();
    }

    public void updateVariantStock(String sku, int newStock) {
        ProductVariant variant = findVariantBySku(sku)
                .orElseThrow(() -> new IllegalArgumentException("Variant with SKU " + sku + " not found"));

        variant.updateStock(newStock);
        this.updatedAt = System.currentTimeMillis();
        updateStockStatus();
    }

    // Review management - FIXED
    public void addReview(Review review) {
        if (review == null) {
            throw new IllegalArgumentException("Review cannot be null");
        }

        // Check if user already reviewed this product
        boolean alreadyReviewed = reviews.stream()
                .anyMatch(r -> r.getUserId().equals(review.getUserId()));
        if (alreadyReviewed) {
            throw new IllegalArgumentException("User already reviewed this product");
        }

        reviews.add(review);
        statistics.addReview(review.getRating());
        this.updatedAt = System.currentTimeMillis();
    }

    public void removeReview(String reviewId) {
        Optional<Review> reviewToRemove = reviews.stream()
                .filter(review -> review.getReviewId().equals(reviewId))
                .findFirst();

        if (reviewToRemove.isPresent()) {
            Review review = reviewToRemove.get();
            reviews.remove(review);
            statistics.removeReview(review.getRating());
            this.updatedAt = System.currentTimeMillis();
        }
    }

    // Business logic
    public boolean isInStock() {
        return variants.stream().anyMatch(ProductVariant::isInStock);
    }

    public Money getLowestPrice() {
        return variants.stream()
                .filter(ProductVariant::isInStock)
                .map(ProductVariant::getPrice)
                .min(Comparator.comparing(Money::getAmount))
                .orElse(basePrice);
    }

    public Money getHighestPrice() {
        return variants.stream()
                .filter(ProductVariant::isInStock)
                .map(ProductVariant::getPrice)
                .max(Comparator.comparing(Money::getAmount))
                .orElse(basePrice);
    }

    public int getTotalStock() {
        return variants.stream()
                .mapToInt(ProductVariant::getStockQuantity)
                .sum();
    }

    public double getAverageRating() {
        return statistics.getAverageRating();
    }

    public int getReviewCount() {
        return statistics.getReviewCount();
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

    // Private helper methods
    private void updateStockStatus() {
        if (this.status == ProductStatus.OUT_OF_STOCK && this.isInStock()) {
            this.markInStock();
        } else if (this.status == ProductStatus.ACTIVE && !this.isInStock()) {
            this.markOutOfStock();
        }
    }

    // Immutable collections
    public List<ProductVariant> getVariants() {
        return Collections.unmodifiableList(variants);
    }

    public List<Review> getReviews() {
        return Collections.unmodifiableList(reviews);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Product product = (Product) o;
        return Objects.equals(productId, product.productId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(productId);
    }

    @Override
    public String toString() {
        return "Product{" +
                "productId=" + productId +
                ", name=" + details.getName() +
                ", status=" + status +
                ", categoryId='" + categoryId + '\'' +
                ", variantCount=" + variants.size() +
                ", reviewCount=" + reviews.size() +
                '}';
    }
}