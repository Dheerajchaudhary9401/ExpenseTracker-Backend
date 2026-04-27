package com.spendsmart.category.resource;

import com.spendsmart.category.dto.CategoryRequest;
import com.spendsmart.category.entity.Category;
import com.spendsmart.category.entity.Category.CategoryType;
import com.spendsmart.category.service.CategoryService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/categories")
@RequiredArgsConstructor
public class CategoryResource {

    private final CategoryService categoryService;

    @PostMapping("/user/{userId}")
    public ResponseEntity<Category> createCategory(
            @PathVariable int userId,
            @Valid @RequestBody CategoryRequest request) {
        return ResponseEntity.ok(categoryService.createCategory(userId, request));
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<List<Category>> getByUser(@PathVariable int userId) {
        return ResponseEntity.ok(categoryService.getByUserId(userId));
    }

    @GetMapping("/{categoryId}")
    public ResponseEntity<Category> getById(@PathVariable int categoryId) {
        return ResponseEntity.ok(categoryService.getCategoryById(categoryId));
    }

    @GetMapping("/user/{userId}/type/{type}")
    public ResponseEntity<List<Category>> getByType(
            @PathVariable int userId,
            @PathVariable CategoryType type) {
        return ResponseEntity.ok(categoryService.getByUserAndType(userId, type));
    }

    @PutMapping("/{categoryId}")
    public ResponseEntity<Category> updateCategory(
            @PathVariable int categoryId,
            @RequestBody CategoryRequest request) {
        return ResponseEntity.ok(categoryService.updateCategory(categoryId, request));
    }

    @DeleteMapping("/{categoryId}")
    public ResponseEntity<String> deleteCategory(@PathVariable int categoryId) {
        categoryService.deleteCategory(categoryId);
        return ResponseEntity.ok("Category deleted");
    }

    @PostMapping("/user/{userId}/init-defaults")
    public ResponseEntity<String> initDefaults(@PathVariable int userId) {
        categoryService.initDefaultCategories(userId);
        return ResponseEntity.ok("Default categories created");
    }

    @PutMapping("/{categoryId}/budget")
    public ResponseEntity<String> setBudget(
            @PathVariable int categoryId,
            @RequestBody Map<String, Double> body) {
        categoryService.setCategoryBudget(categoryId, body.get("budgetLimit"));
        return ResponseEntity.ok("Budget limit updated");
    }

    @GetMapping("/user/{userId}/count")
    public ResponseEntity<Long> getCount(@PathVariable int userId) {
        return ResponseEntity.ok(categoryService.getCategoryCount(userId));
    }
}