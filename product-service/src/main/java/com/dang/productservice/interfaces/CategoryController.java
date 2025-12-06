package com.dang.productservice.interfaces;

import com.dang.productservice.application.commands.CreateCategoryCommand;
import com.dang.productservice.application.commands.UpdateCategoryCommand;
import com.dang.productservice.application.dtos.CategoryResponse;
import com.dang.productservice.application.service.CategoryApplicationService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/categories")
public class CategoryController {

    private final CategoryApplicationService categoryApplicationService;

    public CategoryController(CategoryApplicationService categoryApplicationService) {
        this.categoryApplicationService = categoryApplicationService;
    }

    // ====== CREATE ======

    @PostMapping("/root")
    public ResponseEntity<CategoryResponse> createRootCategory(
            @RequestBody @Valid CreateCategoryCommand command) {

        var category = categoryApplicationService.createRootCategory(command);
        // CategoryResponse.from() sẽ tự map subcategories (lúc này thường là list rỗng)
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(CategoryResponse.from(category));
    }

    @PostMapping("/subcategory")
    public ResponseEntity<CategoryResponse> createSubCategory(
            @RequestBody @Valid CreateCategoryCommand command) {

        var category = categoryApplicationService.createSubCategory(command);
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(CategoryResponse.from(category));
    }

    // ====== READ ======

    /**
     * Lấy toàn bộ category.
     * Nếu CategoryApplicationService.getAllCategories() trả về entity đã có
     * quan hệ cha–con (subcategories), thì CategoryResponse.from() sẽ trả
     * về luôn cây danh mục.
     */
    @GetMapping
    public ResponseEntity<List<CategoryResponse>> getAllCategories() {
        var categories = categoryApplicationService.getAllCategories();
        var responses = categories.stream()
                .map(CategoryResponse::from)
                .toList();
        return ResponseEntity.ok(responses);
    }

    /**
     * Lấy chỉ các root categories (mỗi root kèm theo subtree).
     */
    @GetMapping("/roots")
    public ResponseEntity<List<CategoryResponse>> getRootCategories() {
        var categories = categoryApplicationService.getRootCategories();
        var responses = categories.stream()
                .map(CategoryResponse::from)
                .toList();
        return ResponseEntity.ok(responses);
    }

    /**
     * Lấy 1 category (kèm subcategories nếu JPA load).
     */
    @GetMapping("/{categoryId}")
    public ResponseEntity<CategoryResponse> getCategory(@PathVariable String categoryId) {
        var category = categoryApplicationService.getCategory(categoryId);
        return ResponseEntity.ok(CategoryResponse.from(category));
    }

    /**
     * Lấy riêng danh sách subcategories (1 level) của categoryId.
     */
    @GetMapping("/{categoryId}/subcategories")
    public ResponseEntity<List<CategoryResponse>> getSubcategories(@PathVariable String categoryId) {
        var subcategories = categoryApplicationService.getSubcategories(categoryId);
        var responses = subcategories.stream()
                .map(CategoryResponse::from)
                .toList();
        return ResponseEntity.ok(responses);
    }

    // ====== UPDATE ======

    @PutMapping("/{categoryId}")
    public ResponseEntity<CategoryResponse> updateCategory(
            @PathVariable String categoryId,
            @RequestBody @Valid UpdateCategoryCommand command) {

        var category = categoryApplicationService.updateCategory(categoryId, command);
        return ResponseEntity.ok(CategoryResponse.from(category));
    }

    // ====== DELETE / ACTIVATE / DEACTIVATE ======

    @DeleteMapping("/{categoryId}")
    public ResponseEntity<Void> deleteCategory(@PathVariable String categoryId) {
        categoryApplicationService.deleteCategory(categoryId);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{categoryId}/activate")
    public ResponseEntity<Void> activateCategory(@PathVariable String categoryId) {
        categoryApplicationService.activateCategory(categoryId);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/{categoryId}/deactivate")
    public ResponseEntity<Void> deactivateCategory(@PathVariable String categoryId) {
        categoryApplicationService.deactivateCategory(categoryId);
        return ResponseEntity.ok().build();
    }
}
