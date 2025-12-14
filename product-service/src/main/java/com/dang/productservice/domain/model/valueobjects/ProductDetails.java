package com.dang.productservice.domain.model.valueobjects;

import jakarta.persistence.Access;
import jakarta.persistence.AccessType;
import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;

import java.io.Serializable;
import java.util.Objects;

@Embeddable
@Access(AccessType.FIELD)
public class ProductDetails implements Serializable {

    private static final int NAME_MAX = 255;
    private static final int BRAND_MAX = 100;
    private static final int URL_MAX = 2048;
    private static final int TAGS_MAX = 1000;

    @Column(name = "product_name", nullable = false, length = NAME_MAX)
    private String name;

    @Column(name = "description", length = 4000)
    private String description;

    @Column(name = "image_url", length = URL_MAX)
    private String imageUrl;

    @Column(name = "brand", length = BRAND_MAX)
    private String brand;

    @Column(name = "specifications", length = 4000)
    private String specifications;

    @Column(name = "tags", length = TAGS_MAX)
    private String tags;

    protected ProductDetails() {
        // for JPA
    }

    private ProductDetails(String name,
                           String description,
                           String imageUrl,
                           String brand,
                           String specifications,
                           String tags) {
        this.name = normalizeAndValidateName(name);
        this.description = normalizeNullable(description, 4000, "Description");
        this.imageUrl = normalizeNullable(imageUrl, URL_MAX, "ImageUrl");
        this.brand = normalizeNullable(brand, BRAND_MAX, "Brand");
        this.specifications = normalizeNullable(specifications, 4000, "Specifications");
        this.tags = normalizeNullable(tags, TAGS_MAX, "Tags");
    }

    /**
     * Factory duy nhất, dễ đọc, ít trùng lặp
     */
    public static ProductDetails of(String name,
                                    String description,
                                    String imageUrl,
                                    String brand,
                                    String specifications,
                                    String tags) {
        return new ProductDetails(name, description, imageUrl, brand, specifications, tags);
    }

    /**
     * Convenience factory
     */
    public static ProductDetails basic(String name, String description, String brand) {
        return new ProductDetails(name, description, null, brand, null, null);
    }

    // Getters (không dùng Lombok để rõ ràng, tuỳ bạn giữ @Getter cũng được)
    public String name() {
        return name;
    }

    public String description() {
        return description;
    }

    public String imageUrl() {
        return imageUrl;
    }

    public String brand() {
        return brand;
    }

    public String specifications() {
        return specifications;
    }

    public String tags() {
        return tags;
    }

    // "Withers" (update bất biến)
    public ProductDetails withName(String newName) {
        return new ProductDetails(newName, this.description, this.imageUrl, this.brand, this.specifications, this.tags);
    }

    public ProductDetails withDescription(String newDescription) {
        return new ProductDetails(this.name, newDescription, this.imageUrl, this.brand, this.specifications, this.tags);
    }

    public ProductDetails withImageUrl(String newImageUrl) {
        return new ProductDetails(this.name, this.description, newImageUrl, this.brand, this.specifications, this.tags);
    }

    public ProductDetails withBrand(String newBrand) {
        return new ProductDetails(this.name, this.description, this.imageUrl, newBrand, this.specifications, this.tags);
    }

    public ProductDetails withSpecifications(String newSpecifications) {
        return new ProductDetails(this.name, this.description, this.imageUrl, this.brand, newSpecifications, this.tags);
    }

    public ProductDetails withTags(String newTags) {
        return new ProductDetails(this.name, this.description, this.imageUrl, this.brand, this.specifications, newTags);
    }

    public boolean isEmpty() {
        return name == null || name.isBlank();
    }

    private static String normalizeAndValidateName(String raw) {
        if (raw == null) throw new IllegalArgumentException("Product name cannot be null");

        String normalized = raw.strip();
        if (normalized.isEmpty()) throw new IllegalArgumentException("Product name cannot be empty");
        if (normalized.length() > NAME_MAX) {
            throw new IllegalArgumentException("Product name cannot exceed " + NAME_MAX + " characters");
        }
        return normalized;
    }

    private static String normalizeNullable(String raw, int maxLen, String fieldName) {
        if (raw == null) return null;

        String normalized = raw.strip();
        if (normalized.isEmpty()) return null; // coi blank như null cho gọn dữ liệu

        if (normalized.length() > maxLen) {
            throw new IllegalArgumentException(fieldName + " cannot exceed " + maxLen + " characters");
        }
        return normalized;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof ProductDetails other)) return false;
        return Objects.equals(name, other.name)
                && Objects.equals(description, other.description)
                && Objects.equals(imageUrl, other.imageUrl)
                && Objects.equals(brand, other.brand)
                && Objects.equals(specifications, other.specifications)
                && Objects.equals(tags, other.tags);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, description, imageUrl, brand, specifications, tags);
    }

    @Override
    public String toString() {
        return "ProductDetails{name='%s', brand='%s'}".formatted(name, brand);
    }
}
