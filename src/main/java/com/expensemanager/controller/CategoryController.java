package com.expensemanager.controller;

import com.expensemanager.dto.CategoryRequest;
import com.expensemanager.entity.Category;
import com.expensemanager.security.CustomUserDetails;
import com.expensemanager.service.CategoryService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/categories")
@RequiredArgsConstructor
public class CategoryController {

    private final CategoryService categoryService;

    @GetMapping
    public ResponseEntity<List<Category>> getCategories(@AuthenticationPrincipal CustomUserDetails principal) {
        return ResponseEntity.ok(categoryService.getCategoriesForUser(principal.getUserId()));
    }

    @PostMapping
    public ResponseEntity<Category> createCategory(@AuthenticationPrincipal CustomUserDetails principal,
                                                     @Valid @RequestBody CategoryRequest request) {
        return ResponseEntity.ok(categoryService.createCategory(principal.getUser(), request));
    }

    @PutMapping("/{id}")
    public ResponseEntity<Category> updateCategory(@AuthenticationPrincipal CustomUserDetails principal,
                                                     @PathVariable Long id,
                                                     @Valid @RequestBody CategoryRequest request) {
        return ResponseEntity.ok(categoryService.updateCategory(principal.getUserId(), id, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteCategory(@AuthenticationPrincipal CustomUserDetails principal,
                                                @PathVariable Long id) {
        categoryService.deleteCategory(principal.getUserId(), id);
        return ResponseEntity.noContent().build();
    }
}
