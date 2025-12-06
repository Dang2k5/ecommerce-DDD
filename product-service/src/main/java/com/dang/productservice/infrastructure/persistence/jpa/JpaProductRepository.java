package com.dang.productservice.infrastructure.persistence.jpa;

import com.dang.productservice.domain.model.aggregates.Product;
import com.dang.productservice.domain.model.valueobjects.ProductId;
import com.dang.productservice.domain.model.valueobjects.ProductStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface JpaProductRepository extends JpaRepository<Product, ProductId> {
    // Custom query để tìm bằng ProductId value object
    @Query("SELECT p FROM Product p WHERE p.productId = :productId")
    Optional<Product> findByProductId(@Param("productId") ProductId productId);

    @Query("SELECT p FROM Product p WHERE p.productId.id = :productId")
    Optional<Product> findByProductIdString(@Param("productId") String productId);

    Optional<Product> findByVariants_Sku(String sku);

    @Query("SELECT p FROM Product p WHERE LOWER(p.details.name) LIKE LOWER(CONCAT('%', :name, '%'))")
    Page<Product> findByNameContaining(@Param("name") String name, Pageable pageable);

    Page<Product> findByCategoryId(String categoryId, Pageable pageable);
    Page<Product> findByStatus(ProductStatus status, Pageable pageable);

    @Query("SELECT COUNT(p) FROM Product p WHERE p.productId = :productId")
    boolean existsByProductId(@Param("productId") ProductId productId);

    @Query("SELECT COUNT(p) FROM Product p WHERE p.categoryId = :categoryId")
    long countByCategoryId(@Param("categoryId") String categoryId);

    @Query("SELECT p FROM Product p WHERE " +
            "(:name IS NULL OR LOWER(p.details.name) LIKE LOWER(CONCAT('%', :name, '%'))) AND " +
            "(:categoryId IS NULL OR p.categoryId = :categoryId) AND " +
            "(:brand IS NULL OR LOWER(p.details.brand) LIKE LOWER(CONCAT('%', :brand, '%'))) AND " +
            "(:minPrice IS NULL OR p.basePrice.amount >= :minPrice) AND " +
            "(:maxPrice IS NULL OR p.basePrice.amount <= :maxPrice) AND " +
            "(:inStock IS NULL OR (:inStock = true AND p.status = 'ACTIVE' AND EXISTS (SELECT v FROM p.variants v WHERE v.stockQuantity > 0)) OR " +
            "(:inStock = false AND (p.status = 'OUT_OF_STOCK' OR NOT EXISTS (SELECT v FROM p.variants v WHERE v.stockQuantity > 0))))")
    Page<Product> findByFilters(@Param("name") String name,
                                @Param("categoryId") String categoryId,
                                @Param("brand") String brand,
                                @Param("minPrice") Double minPrice,
                                @Param("maxPrice") Double maxPrice,
                                @Param("inStock") Boolean inStock,
                                Pageable pageable);
}

