package com.spendsmart.category.dto;

import com.spendsmart.category.entity.Category.CategoryType;
import jakarta.validation.constraints.*;
import lombok.Data;

@Data
public class CategoryRequest {

    @NotBlank(message = "Category name is required")
    private String name;

    @NotNull(message = "Category type is required")
    private CategoryType type;

    private String icon = "📁";

    private String colorCode = "#3B82F6";

    private double budgetLimit = 0.0;
}