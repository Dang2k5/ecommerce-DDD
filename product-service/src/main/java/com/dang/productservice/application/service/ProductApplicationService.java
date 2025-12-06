package com.dang.productservice.application.service;

import com.dang.productservice.application.commands.CreateProductCommand;
import com.dang.productservice.application.commands.UpdateProductCommand;
import com.dang.productservice.application.exceptions.ProductNotFoundException;
import com.dang.productservice.domain.model.aggregates.Product;
import com.dang.productservice.domain.model.valueobjects.ProductId;
import com.dang.productservice.domain.repository.ProductRepository;
import com.dang.productservice.domain.service.ProductDomainService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
@Transactional
public class ProductApplicationService {

    private final ProductRepository productRepository;
    private final ProductDomainService productDomainService;
    private final CategoryApplicationService categoryApplicationService;

    public ProductApplicationService(ProductRepository productRepository,
                                     ProductDomainService productDomainService,
                                     CategoryApplicationService categoryApplicationService) {
        this.productRepository = productRepository;
        this.productDomainService = productDomainService;
        this.categoryApplicationService = categoryApplicationService;
    }

    public Product createProduct(CreateProductCommand command) {
        // Validate category exists
        categoryApplicationService.validateCategoryExists(command.getCategoryId());

        Product product;

        if (command.getImageUrl() != null && command.getSpecifications() != null) {
            // Create full product với tất cả thông tin
            product = productDomainService.createFullProduct(
                    command.getName(),
                    command.getDescription(),
                    command.getBasePrice(),
                    command.getCategoryId(),
                    command.getBrand(),
                    command.getImageUrl(),
                    command.getSpecifications(),
                    command.getTags()
            );
        } else if (command.getImageUrl() != null) {
            // Create product với image
            product = productDomainService.createProductWithImage(
                    command.getName(),
                    command.getDescription(),
                    command.getBasePrice(),
                    command.getCategoryId(),
                    command.getBrand(),
                    command.getImageUrl()
            );
        } else {
            // Create basic product
            product = productDomainService.createBasicProduct(
                    command.getName(),
                    command.getDescription(),
                    command.getBasePrice(),
                    command.getCategoryId(),
                    command.getBrand()
            );
        }

        // Add variants if any
        if (command.getVariants() != null) {
            command.getVariants().forEach(variantCommand ->
                    productDomainService.addVariantToProduct(
                            product,
                            variantCommand.getSku(),
                            variantCommand.getSize(),
                            variantCommand.getColor(),
                            variantCommand.getMaterial(),
                            variantCommand.getPrice(),
                            variantCommand.getInitialStock()
                    )
            );
        }

        return productRepository.save(product);
    }

    public Product updateProduct(String productId, UpdateProductCommand command) {
        Product product = getProductOrThrow(productId);

        // Update product details nếu có thay đổi
        if (command.getName() != null || command.getDescription() != null ||
                command.getImageUrl() != null || command.getBrand() != null ||
                command.getSpecifications() != null || command.getTags() != null) {

            productDomainService.updateProductDetails(
                    product,
                    command.getName(),
                    command.getDescription(),
                    command.getImageUrl(),
                    command.getBrand(),
                    command.getSpecifications(),
                    command.getTags()
            );
        }

        if (command.getBasePrice() != null) {
            productDomainService.updateProductPrice(product, command.getBasePrice());
        }

        if (command.getCategoryId() != null) {
            categoryApplicationService.validateCategoryExists(command.getCategoryId());
            product.changeCategory(command.getCategoryId());
        }

        return productRepository.save(product);
    }

    // ====== CÁCH 2: HÀM QUERY-FRIENDLY DÙNG OPTIONAL (KHÔNG NÉM EXCEPTION) ======
    @Transactional(readOnly = true)
    public Optional<Product> findProduct(String productId) {
        return productRepository.findById(ProductId.fromString(productId));
    }

    // ====== HÀM INTERNAL DÙNG CHO COMMAND (CÓ NÉM EXCEPTION) ======
    @Transactional(readOnly = true)
    protected Product getProductOrThrow(String productId) {
        return productRepository.findById(ProductId.fromString(productId))
                .orElseThrow(() -> new ProductNotFoundException("Product not found: " + productId));
    }

    public void deleteProduct(String productId) {
        Product product = getProductOrThrow(productId);

        // Nếu sau này có rule stock thì check ở đây (hiện tại anh đã bỏ rule đó đi)
        // if (product.getTotalStock() > 0) {
        //     throw new IllegalStateException("Cannot delete product with existing stock");
        // }

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
}
