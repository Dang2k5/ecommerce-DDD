package com.dang.productservice.infrastructure.persistence.jpa;

import com.dang.productservice.domain.model.entities.Category;
import com.dang.productservice.domain.model.valueobjects.CategoryId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface JpaCategoryRepository extends JpaRepository<Category, CategoryId> {

    Optional<Category> findBySlug(String slug);

    List<Category> findByParentId(CategoryId parentId);

    @Query("select c from Category c where c.parent is null")
    List<Category> findRootCategories();

    List<Category> findByActiveTrue();

    boolean existsBySlug(String slug);

    @Query("select (count(c) > 0) from Category c where c.parent.id = :categoryId and c.active = true")
    boolean hasActiveSubcategories(@Param("categoryId") CategoryId categoryId);

    // ⚠️ chỉ dùng được nếu Product.categoryId là CategoryId
    @Query("select (count(p) > 0) from Product p where p.categoryId = :categoryId")
    boolean hasProducts(@Param("categoryId") CategoryId categoryId);
}
