package com.spendsmart.category.repository;

import com.spendsmart.category.entity.Category;
import com.spendsmart.category.entity.Category.CategoryType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface CategoryRepository extends JpaRepository<Category, Integer> {

    List<Category> findByUserId(int userId);

    List<Category> findByUserIdAndType(int userId, CategoryType type);

    Optional<Category> findByCategoryId(int categoryId);

    Optional<Category> findByUserIdAndName(int userId, String name);

    List<Category> findByIsDefault(boolean isDefault);

    long countByUserId(int userId);

    void deleteByUserId(int userId);

    void deleteByCategoryId(int categoryId);
}