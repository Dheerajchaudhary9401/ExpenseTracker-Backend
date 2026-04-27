package com.spendsmart.category.service.impl;

import com.spendsmart.category.dto.CategoryRequest;
import com.spendsmart.category.entity.Category;
import com.spendsmart.category.entity.Category.CategoryType;
import com.spendsmart.category.repository.CategoryRepository;
import com.spendsmart.category.service.CategoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;

@Service
@RequiredArgsConstructor
public class CategoryServiceImpl implements CategoryService {

    private final CategoryRepository categoryRepository;

    @Override
    public Category createCategory(int userId, CategoryRequest request) {
        // Check if category name already exists for this user
        categoryRepository.findByUserIdAndName(userId, request.getName())
                .ifPresent(c -> {
                    throw new RuntimeException("Category already exists");
                });

        Category category = Category.builder()
                .userId(userId)
                .name(request.getName())
                .type(request.getType())
                .icon(request.getIcon())
                .colorCode(request.getColorCode())
                .budgetLimit(request.getBudgetLimit())
                .isDefault(false)
                .build();

        return categoryRepository.save(category);
    }

    @Override
    public List<Category> getByUserId(int userId) {
        return categoryRepository.findByUserId(userId);
    }

    @Override
    public Category getCategoryById(int categoryId) {
        return categoryRepository.findByCategoryId(categoryId)
                .orElseThrow(() -> new RuntimeException("Category not found"));
    }

    @Override
    public List<Category> getByUserAndType(int userId, CategoryType type) {
        return categoryRepository.findByUserIdAndType(userId, type);
    }

    @Override
    public Category updateCategory(int categoryId, CategoryRequest request) {
        Category category = getCategoryById(categoryId);

        if (request.getName() != null) category.setName(request.getName());
        if (request.getIcon() != null) category.setIcon(request.getIcon());
        if (request.getColorCode() != null) category.setColorCode(request.getColorCode());
        if (request.getBudgetLimit() >= 0) category.setBudgetLimit(request.getBudgetLimit());

        return categoryRepository.save(category);
    }

    @Override
    @Transactional
    public void deleteCategory(int categoryId) {
        categoryRepository.deleteByCategoryId(categoryId);
    }

    @Override
    public List<Category> getDefaultCategories() {
        return categoryRepository.findByIsDefault(true);
    }

    @Override
    @Transactional
    public void initDefaultCategories(int userId) {
        // Expense categories
        createDefaultCategory(userId, "Food", CategoryType.EXPENSE, "🍔", "#EF4444");
        createDefaultCategory(userId, "Transport", CategoryType.EXPENSE, "🚗", "#F59E0B");
        createDefaultCategory(userId, "Shopping", CategoryType.EXPENSE, "🛍️", "#8B5CF6");
        createDefaultCategory(userId, "Bills", CategoryType.EXPENSE, "💡", "#3B82F6");
        createDefaultCategory(userId, "Health", CategoryType.EXPENSE, "🏥", "#10B981");
        createDefaultCategory(userId, "Entertainment", CategoryType.EXPENSE, "🎬", "#EC4899");

        // Income categories
        createDefaultCategory(userId, "Salary", CategoryType.INCOME, "💰", "#059669");
        createDefaultCategory(userId, "Freelance", CategoryType.INCOME, "💼", "#0891B2");
        createDefaultCategory(userId, "Investment", CategoryType.INCOME, "📈", "#7C3AED");
        createDefaultCategory(userId, "Gift", CategoryType.INCOME, "🎁", "#DB2777");
    }

    private void createDefaultCategory(int userId, String name, CategoryType type,
                                       String icon, String color) {
        Category category = Category.builder()
                .userId(userId)
                .name(name)
                .type(type)
                .icon(icon)
                .colorCode(color)
                .budgetLimit(0.0)
                .isDefault(true)
                .build();
        categoryRepository.save(category);
    }

    @Override
    public void setCategoryBudget(int categoryId, double budgetLimit) {
        Category category = getCategoryById(categoryId);
        category.setBudgetLimit(budgetLimit);
        categoryRepository.save(category);
    }

    @Override
    public long getCategoryCount(int userId) {
        return categoryRepository.countByUserId(userId);
    }
}