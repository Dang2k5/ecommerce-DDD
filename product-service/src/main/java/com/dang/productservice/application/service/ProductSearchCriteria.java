package com.dang.productservice.application.service;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
public class ProductSearchCriteria {

    private String name;
    private String categoryId;
    private String brand;

    private BigDecimal minPrice;
    private BigDecimal maxPrice;

    private Boolean inStock;

    public ProductSearchCriteria(String name,
                                 String categoryId,
                                 String brand,
                                 BigDecimal minPrice,
                                 BigDecimal maxPrice,
                                 Boolean inStock) {
        this.name = normalize(name);
        this.categoryId = normalize(categoryId);
        this.brand = normalize(brand);
        this.minPrice = minPrice;
        this.maxPrice = maxPrice;
        this.inStock = inStock;
        validatePriceRange(minPrice, maxPrice);
    }

    public String normalizedName() {
        return normalize(name);
    }

    public String normalizedCategoryId() {
        return normalize(categoryId);
    }

    public String normalizedBrand() {
        return normalize(brand);
    }

    public boolean hasPriceFilter() {
        return minPrice != null || maxPrice != null;
    }

    private static void validatePriceRange(BigDecimal min, BigDecimal max) {
        if (min != null && min.signum() < 0) {
            throw new IllegalArgumentException("minPrice cannot be negative");
        }
        if (max != null && max.signum() < 0) {
            throw new IllegalArgumentException("maxPrice cannot be negative");
        }
        if (min != null && max != null && min.compareTo(max) > 0) {
            throw new IllegalArgumentException("minPrice cannot be greater than maxPrice");
        }
    }

    private static String normalize(String s) {
        if (s == null) return null;
        String v = s.strip();
        return v.isEmpty() ? null : v;
    }
}
