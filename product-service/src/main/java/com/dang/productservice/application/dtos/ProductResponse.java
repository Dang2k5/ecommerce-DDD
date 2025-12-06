package com.dang.productservice.application.dtos;

import com.dang.productservice.domain.model.aggregates.Product;
import com.dang.productservice.domain.model.valueobjects.Money;
import lombok.Getter;
import lombok.Setter;

import java.util.List;
import java.util.stream.Collectors;

@Getter
@Setter
public class ProductResponse {
    private String productId;
    private String name;
    private String description;
    private Money basePrice;
    private String categoryId;
    private String brand;
    private String imageUrl;
    private String specifications;
    private String tags;
    private String status;
    private boolean inStock;
    private Integer totalStock;
    private Double averageRating;
    private Integer totalReviews;
    private List<ProductVariantResponse> variants;

    public static ProductResponse from(Product product) {
        ProductResponse response = new ProductResponse();
        response.productId = product.getProductId().getId();
        response.name = product.getDetails().getName();
        response.description = product.getDetails().getDescription();
        response.basePrice = product.getBasePrice();
        response.categoryId = product.getCategoryId();
        response.brand = product.getDetails().getBrand();
        response.imageUrl = product.getDetails().getImageUrl();
        response.specifications = product.getDetails().getSpecifications();
        response.tags = product.getDetails().getTags();
        response.status = product.getStatus().name();
        response.inStock = product.isInStock();
        response.totalStock = product.getTotalStock();
        response.averageRating = product.getStatistics().getAverageRating();
        response.totalReviews = product.getStatistics().getTotalReviews();
        response.variants = product.getVariants().stream()
                .map(ProductVariantResponse::from)
                .collect(Collectors.toList());

        return response;
    }
}
