package com.dang.productservice.application.service;

import com.dang.productservice.application.commands.CreateProductCommand;
import com.dang.productservice.application.commands.ProductVariantCommand;
import com.dang.productservice.application.commands.UpdateProductCommand;
import com.dang.productservice.application.exceptions.CategoryNotFoundException;
import com.dang.productservice.application.exceptions.ProductNotFoundException;
import com.dang.productservice.domain.model.aggregates.Product;
import com.dang.productservice.domain.model.valueobjects.CategoryId;
import com.dang.productservice.domain.model.valueobjects.Money;
import com.dang.productservice.domain.model.valueobjects.ProductDetails;
import com.dang.productservice.domain.model.valueobjects.ProductId;
import com.dang.productservice.domain.repository.CategoryRepository;
import com.dang.productservice.domain.repository.ProductRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

/**
 * Application service cho Product.
 * <p>
 * Clean-up:
 * - Bỏ DomainService vì aggregate Product đã encapsulate đầy đủ invariants (price, details, variant, stock).
 * - Tránh gọi chéo ApplicationService (Product -> CategoryApplicationService) để giảm coupling.
 */
@Service
@Transactional
public class ProductApplicationService {

    private final ProductRepository productRepository;
    private final CategoryRepository categoryRepository;

    public ProductApplicationService(ProductRepository productRepository,
                                     CategoryRepository categoryRepository) {
        this.productRepository = productRepository;
        this.categoryRepository = categoryRepository;
    }

    public Product createProduct(CreateProductCommand command) {
        CategoryId categoryId = requireExistingCategory(command.getCategoryId());

        Money basePrice = Money.of(command.getBasePriceAmount(), command.getBasePriceCurrency());

        ProductDetails details = ProductDetails.of(
                command.getName(),
                command.getDescription(),
                command.getImageUrl(),
                command.getBrand(),
                command.getSpecifications(),
                command.getTags()
        );

        Product product = Product.create(details, basePrice, categoryId);

        if (command.getVariants() != null && !command.getVariants().isEmpty()) {
            for (ProductVariantCommand v : command.getVariants()) {
                product.createVariant(
                        v.getSku(),
                        v.getSize(),
                        v.getColor(),
                        v.getMaterial(),
                        Money.of(v.getPriceAmount(), v.getPriceCurrency()),
                        v.getInitialStock()
                );
            }
        } else if (command.getInitialStock() != null) {
            // Rule tối thiểu: nếu không có variants mà có initialStock -> tạo 1 variant "DEFAULT".
            // Nếu bạn không muốn auto-create, hãy xóa block này và để strict (throw) thay vì tạo default.
            product.createVariant(
                    "DEFAULT-" + product.getProductId().value(),
                    Money.of(command.getBasePriceAmount(), command.getBasePriceCurrency()),
                    command.getInitialStock()
            );
        }

        return productRepository.save(product);
    }

    public Product updateProduct(String productId, UpdateProductCommand command) {
        Product product = getProductOrThrow(productId);

        // patch details
        if (hasAnyDetailsChange(command)) {
            var current = product.getDetails();
            ProductDetails updated = ProductDetails.of(
                    pick(command.getName(), current.name()),
                    pick(command.getDescription(), current.description()),
                    pick(command.getImageUrl(), current.imageUrl()),
                    pick(command.getBrand(), current.brand()),
                    pick(command.getSpecifications(), current.specifications()),
                    pick(command.getTags(), current.tags())
            );
            product.updateDetails(updated);
        }

        // patch base price: phải đủ amount + currency
        if (command.getBasePriceAmount() != null || command.getBasePriceCurrency() != null) {
            if (command.getBasePriceAmount() == null || command.getBasePriceCurrency() == null) {
                throw new IllegalArgumentException("basePriceAmount and basePriceCurrency must be provided together");
            }
            product.updateBasePrice(Money.of(command.getBasePriceAmount(), command.getBasePriceCurrency()));
        }

        // patch category
        if (command.getCategoryId() != null && !command.getCategoryId().isBlank()) {
            CategoryId newCategoryId = requireExistingCategory(command.getCategoryId());
            product.changeCategory(newCategoryId);
        }

        return productRepository.save(product);
    }

    @Transactional(readOnly = true)
    public Optional<Product> findProduct(String productId) {
        return productRepository.findById(ProductId.of(requireId(productId)));
    }

    @Transactional(readOnly = true)
    protected Product getProductOrThrow(String productId) {
        return productRepository.findById(ProductId.of(requireId(productId)))
                .orElseThrow(() -> new ProductNotFoundException("Product not found: " + productId));
    }

    public void deleteProduct(String productId) {
        Product product = getProductOrThrow(productId);
        productRepository.deleteById(product.getProductId());
    }

    public void activateProduct(String productId) {
        Product product = getProductOrThrow(productId);
        product.activate();
        productRepository.save(product);
    }

    public void deactivateProduct(String productId) {
        Product product = getProductOrThrow(productId);
        product.deactivate();
        productRepository.save(product);
    }

    public void markOutOfStock(String productId) {
        Product product = getProductOrThrow(productId);
        product.markOutOfStock();
        productRepository.save(product);
    }

    // ===== Helpers =====

    private CategoryId requireExistingCategory(String rawCategoryId) {
        CategoryId categoryId = CategoryId.of(requireId(rawCategoryId));
        if (!categoryRepository.existsById(categoryId)) {
            throw new CategoryNotFoundException("Category not found: " + rawCategoryId);
        }
        return categoryId;
    }

    private static boolean hasAnyDetailsChange(UpdateProductCommand c) {
        return c.getName() != null
                || c.getDescription() != null
                || c.getImageUrl() != null
                || c.getBrand() != null
                || c.getSpecifications() != null
                || c.getTags() != null;
    }

    private static String requireId(String id) {
        if (id == null || id.isBlank()) {
            throw new IllegalArgumentException("Id cannot be null or empty");
        }
        return id.strip();
    }

    private static String pick(String incoming, String current) {
        if (incoming == null) return current;
        String v = incoming.strip();
        return v.isEmpty() ? current : v;
    }
}
