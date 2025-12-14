package com.dang.productservice.interfaces;

import com.dang.productservice.application.commands.CreateCategoryCommand;
import com.dang.productservice.application.commands.UpdateCategoryCommand;
import com.dang.productservice.application.dtos.CategoryResponse;
import com.dang.productservice.application.service.CategoryApplicationService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;
import java.util.List;

@Validated
@RestController
@RequestMapping("/api/categories")
public class CategoryController {

    private final CategoryApplicationService categoryApp;

    public CategoryController(CategoryApplicationService categoryApp) {
        this.categoryApp = categoryApp;
    }

    // ===== CREATE =====
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    @PostMapping("/root")
    public ResponseEntity<CategoryResponse> createRootCategory(
            @RequestBody @Valid CreateCategoryCommand command,
            UriComponentsBuilder uriBuilder
    ) {
        // Map DTO bên trong transaction (service) để tránh LazyInitializationException
        var response = categoryApp.createRootCategoryResponse(command, 5);

        URI location = uriBuilder.path("/api/categories/{id}")
                .buildAndExpand(response.getCategoryId())
                .toUri();

        return ResponseEntity.created(location).body(response);
    }
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    @PostMapping("/{parentId}/children")
    public ResponseEntity<CategoryResponse> createSubCategory(
            @PathVariable String parentId,
            @RequestBody @Valid CreateCategoryCommand command,
            UriComponentsBuilder uriBuilder
    ) {
        command.setParentId(parentId);

        var response = categoryApp.createSubCategoryResponse(command, 5);

        URI location = uriBuilder.path("/api/categories/{id}")
                .buildAndExpand(response.getCategoryId())
                .toUri();

        return ResponseEntity.created(location).body(response);
    }

    // ===== READ =====
    @GetMapping("/{categoryId}")
    public ResponseEntity<CategoryResponse> getCategory(@PathVariable String categoryId) {
        return ResponseEntity.ok(categoryApp.getCategoryResponse(categoryId, 5));
    }

    @GetMapping("/roots")
    public ResponseEntity<List<CategoryResponse>> getRootCategories() {
        return ResponseEntity.ok(categoryApp.getRootCategoryResponses());
    }

    @GetMapping("/{categoryId}/children")
    public ResponseEntity<List<CategoryResponse>> getSubcategories(@PathVariable String categoryId) {
        return ResponseEntity.ok(categoryApp.getSubcategoryResponses(categoryId, 1));
    }

    // ===== UPDATE =====
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    @PutMapping("/{categoryId}")
    public ResponseEntity<CategoryResponse> updateCategory(
            @PathVariable String categoryId,
            @RequestBody @Valid UpdateCategoryCommand command
    ) {
        return ResponseEntity.ok(categoryApp.updateCategoryResponse(categoryId, command, 5));
    }

    // ===== DELETE / STATUS =====
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    @DeleteMapping("/{categoryId}")
    public ResponseEntity<Void> deleteCategory(@PathVariable String categoryId) {
        categoryApp.deleteCategory(categoryId);
        return ResponseEntity.noContent().build();
    }
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    @PostMapping("/{categoryId}/activate")
    public ResponseEntity<Void> activateCategory(@PathVariable String categoryId) {
        categoryApp.activateCategory(categoryId);
        return ResponseEntity.noContent().build();
    }
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    @PostMapping("/{categoryId}/deactivate")
    public ResponseEntity<Void> deactivateCategory(@PathVariable String categoryId) {
        categoryApp.deactivateCategory(categoryId);
        return ResponseEntity.noContent().build();
    }
}
