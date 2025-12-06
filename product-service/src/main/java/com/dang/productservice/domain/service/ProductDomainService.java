package com.dang.productservice.domain.service;

import com.dang.productservice.domain.model.aggregates.Product;
import com.dang.productservice.domain.model.entities.ProductVariant;
import com.dang.productservice.domain.model.valueobjects.Money;
import com.dang.productservice.domain.model.valueobjects.ProductDetails;
import com.dang.productservice.domain.model.valueobjects.ProductId;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

@Service
public class ProductDomainService {

    public Product createBasicProduct(String name, String description, Money basePrice,
                                      String categoryId, String brand) {
        validateProductCreation(name, basePrice, categoryId);

        ProductDetails details = ProductDetails.create(name, description, brand);
        return Product.create(details, basePrice, categoryId);
    }

    public Product createProductWithImage(String name, String description, Money basePrice,
                                          String categoryId, String brand, String imageUrl) {
        validateProductCreation(name, basePrice, categoryId);

        ProductDetails details = ProductDetails.createWithImage(name, description, imageUrl, brand);
        return Product.create(details, basePrice, categoryId);
    }

    public Product createFullProduct(String name, String description, Money basePrice,
                                     String categoryId, String brand,
                                     String imageUrl, String specifications, String tags) {
        validateProductCreation(name, basePrice, categoryId);

        ProductDetails details = ProductDetails.createFull(name, description, imageUrl,
                brand, specifications, tags);
        return Product.create(details, basePrice, categoryId);
    }

    public void addVariantToProduct(Product product, String sku, String size, String color,
                                    String material, Money price, Integer stockQuantity) {
        validateVariantCreation(sku, price, stockQuantity);

        ProductVariant variant = new ProductVariant(
                sku,
                size,
                color,
                material,
                price,
                stockQuantity
        );

        product.addVariant(variant);
    }

    public void updateProductPrice(Product product, Money newPrice) {
        if (newPrice == null) {
            throw new IllegalArgumentException("Price cannot be null");
        }
        if (newPrice.getAmount().compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("Price cannot be negative");
        }
        product.updateBasePrice(newPrice);
    }

    public void updateProductDetails(Product product, String name, String description,
                                     String imageUrl, String brand, String specifications, String tags) {
        ProductDetails newDetails = ProductDetails.createFull(
                name != null ? name : product.getDetails().getName(),
                description != null ? description : product.getDetails().getDescription(),
                imageUrl != null ? imageUrl : product.getDetails().getImageUrl(),
                brand != null ? brand : product.getDetails().getBrand(),
                specifications != null ? specifications : product.getDetails().getSpecifications(),
                tags != null ? tags : product.getDetails().getTags()
        );

        product.updateDetails(newDetails);
    }

    private void validateProductCreation(String name, Money basePrice, String categoryId) {
        if (name == null || name.trim().isEmpty()) {
            throw new IllegalArgumentException("Product name cannot be null or empty");
        }
        if (basePrice == null) {
            throw new IllegalArgumentException("Product base price cannot be null");
        }
        if (basePrice.getAmount().compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("Product base price cannot be negative");
        }
        if (categoryId == null || categoryId.trim().isEmpty()) {
            throw new IllegalArgumentException("Category ID cannot be null or empty");
        }
    }

    private void validateVariantCreation(String sku, Money price, Integer stockQuantity) {
        if (sku == null || sku.trim().isEmpty()) {
            throw new IllegalArgumentException("Variant SKU cannot be null or empty");
        }
        if (price == null) {
            throw new IllegalArgumentException("Variant price cannot be null");
        }
        if (price.getAmount().compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("Variant price cannot be negative");
        }
        if (stockQuantity == null || stockQuantity < 0) {
            throw new IllegalArgumentException("Stock quantity cannot be negative");
        }
    }

    public boolean isProductNameUnique(String name, ProductId excludeProductId) {
        // Implementation sẽ được inject từ repository
        // Trong thực tế, bạn sẽ gọi repository để kiểm tra
        return true;
    }
}