package com.dang.productservice.application.dtos;

import com.dang.productservice.domain.model.aggregates.Product;
import lombok.Getter;

import java.math.BigDecimal;
import java.util.List;

@Getter
public class ProductResponse {

    private String productId;
    private String name;
    private String description;

    private BigDecimal basePriceAmount;
    private String basePriceCurrency;

    private String categoryId;
    private String brand;
    private String imageUrl;
    private String specifications;
    private String tags;

    private String status;
    private boolean inStock;
    private int totalStock;

    private List<ProductVariantResponse> variants;

    private ProductResponse() {
    }

    public static ProductResponse from(Product product) {
        ProductResponse res = new ProductResponse();

        res.productId = product.getProductId().value();

        res.name = product.getDetails().name();
        res.description = product.getDetails().description();
        res.brand = product.getDetails().brand();
        res.imageUrl = product.getDetails().imageUrl();
        res.specifications = product.getDetails().specifications();
        res.tags = product.getDetails().tags();

        res.basePriceAmount = product.getBasePrice().amount();
        res.basePriceCurrency = product.getBasePrice().currency();

        // ✅ CategoryId -> String
        res.categoryId = product.getCategoryId().value();

        res.status = product.getStatus().name();
        res.inStock = product.isInStock();
        res.totalStock = product.getTotalStock();

        res.variants = product.getVariants() == null
                ? List.of()
                : product.getVariants().stream()
                .map(ProductVariantResponse::from)
                .toList();

        return res;
    }
}
