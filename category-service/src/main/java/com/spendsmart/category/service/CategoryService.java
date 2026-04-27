package com.spendsmart.category.service;

import com.spendsmart.category.dto.CategoryRequest;
import com.spendsmart.category.entity.Category;
import com.spendsmart.category.entity.Category.CategoryType;
import java.util.List;

public interface CategoryService {

    Category createCategory(int userId, CategoryRequest request);

    List<Category> getByUserId(int userId);

    Category getCategoryById(int categoryId);

    List<Category> getByUserAndType(int userId, CategoryType type);

    Category updateCategory(int categoryId, CategoryRequest request);

    void deleteCategory(int categoryId);

    List<Category> getDefaultCategories();

    void initDefaultCategories(int userId);

    void setCategoryBudget(int categoryId, double budgetLimit);

    long getCategoryCount(int userId);
}