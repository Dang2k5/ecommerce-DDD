package com.dang.productservice.infrastructure.persistence.jpa;

import com.dang.productservice.domain.model.aggregates.Product;
import com.dang.productservice.domain.model.entities.ProductVariant;
import com.dang.productservice.domain.model.valueobjects.CategoryId;
import com.dang.productservice.domain.model.valueobjects.ProductId;
import com.dang.productservice.domain.model.valueobjects.ProductStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.Locale;
import java.util.Optional;

@Repository
public interface JpaProductRepository extends JpaRepository<Product, ProductId> {

    // ===== Simple derived queries =====
    Optional<Product> findByVariants_Sku(String sku);

    /**
     * Internal query: param nameLike đã là "%...%" và đã lowercase
     * => tránh lower(concat('%', :name, '%')) gây lower(bytea)
     */
    @Query("""
        select p
        from Product p
        where (:nameLike is null or lower(p.details.name) like :nameLike)
    """)
    Page<Product> findByNameLike(@Param("nameLike") String nameLike, Pageable pageable);

    /**
     * Public API giữ nguyên như bạn đang dùng:
     * - nhận name raw
     * - tự build pattern "%...%" + lowercase
     */
    default Page<Product> findByNameContaining(String name, Pageable pageable) {
        return findByNameLike(toLikePattern(name), pageable);
    }

    Page<Product> findByCategoryId(CategoryId categoryId, Pageable pageable);

    Page<Product> findByStatus(ProductStatus status, Pageable pageable);

    long countByCategoryId(CategoryId categoryId);

    // ===== Filter search =====
    @Query("""
        select p
        from Product p
        where
            (:nameLike is null or lower(p.details.name) like :nameLike)
        and (:categoryId is null or p.categoryId = :categoryId)
        and (:brandLike is null or lower(p.details.brand) like :brandLike)
        and (:minPrice is null or p.basePrice.amount >= :minPrice)
        and (:maxPrice is null or p.basePrice.amount <= :maxPrice)
        and (
            :inStock is null
            or (
                :inStock = true
                and exists (
                    select 1
                    from ProductVariant v
                    where v.product = p and v.stockQuantity > 0
                )
            )
            or (
                :inStock = false
                and not exists (
                    select 1
                    from ProductVariant v
                    where v.product = p and v.stockQuantity > 0
                )
            )
        )
    """)
    Page<Product> findByFiltersLike(@Param("nameLike") String nameLike,
                                    @Param("categoryId") CategoryId categoryId,
                                    @Param("brandLike") String brandLike,
                                    @Param("minPrice") BigDecimal minPrice,
                                    @Param("maxPrice") BigDecimal maxPrice,
                                    @Param("inStock") Boolean inStock,
                                    Pageable pageable);

    /**
     * Public API giữ nguyên signature như bạn đang dùng.
     * Wrapper sẽ build like pattern để query không phải lower(param).
     */
    default Page<Product> findByFilters(String name,
                                        CategoryId categoryId,
                                        String brand,
                                        BigDecimal minPrice,
                                        BigDecimal maxPrice,
                                        Boolean inStock,
                                        Pageable pageable) {
        return findByFiltersLike(
                toLikePattern(name),
                categoryId,
                toLikePattern(brand),
                minPrice,
                maxPrice,
                inStock,
                pageable
        );
    }

    private static String toLikePattern(String raw) {
        if (raw == null) return null;
        String v = raw.strip();
        if (v.isEmpty()) return null;
        return "%" + v.toLowerCase(Locale.ROOT) + "%";
    }
}
