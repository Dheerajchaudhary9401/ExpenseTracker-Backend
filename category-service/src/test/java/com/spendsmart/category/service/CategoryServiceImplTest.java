package com.spendsmart.category.service;

import com.spendsmart.category.dto.CategoryRequest;
import com.spendsmart.category.entity.Category;
import com.spendsmart.category.entity.Category.CategoryType;
import com.spendsmart.category.repository.CategoryRepository;
import com.spendsmart.category.service.impl.CategoryServiceImpl;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import java.util.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("CategoryServiceImpl - Unit Tests")
class CategoryServiceImplTest {

    @Mock private CategoryRepository categoryRepository;
    @InjectMocks private CategoryServiceImpl categoryService;

    private Category sampleCategory;
    private CategoryRequest categoryRequest;

    @BeforeEach
    void setUp() {
        sampleCategory = Category.builder()
                .categoryId(1).userId(10).name("Food")
                .type(CategoryType.EXPENSE).icon("🍔").colorCode("#EF4444")
                .budgetLimit(5000.0).isDefault(false).build();

        categoryRequest = new CategoryRequest();
        categoryRequest.setName("Food");
        categoryRequest.setType(CategoryType.EXPENSE);
        categoryRequest.setIcon("🍔");
        categoryRequest.setColorCode("#EF4444");
        categoryRequest.setBudgetLimit(5000.0);
    }

    @Test @DisplayName("createCategory() - Should save and return new category")
    void createCategory_Success() {
        when(categoryRepository.findByUserIdAndName(10, "Food")).thenReturn(Optional.empty());
        when(categoryRepository.save(any(Category.class))).thenReturn(sampleCategory);
        Category result = categoryService.createCategory(10, categoryRequest);
        assertNotNull(result);
        assertEquals("Food", result.getName());
        verify(categoryRepository, times(1)).save(any(Category.class));
    }

    @Test @DisplayName("createCategory() - Should throw when category name already exists for user")
    void createCategory_DuplicateName_Throws() {
        when(categoryRepository.findByUserIdAndName(10, "Food")).thenReturn(Optional.of(sampleCategory));
        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> categoryService.createCategory(10, categoryRequest));
        assertEquals("Category already exists", ex.getMessage());
        verify(categoryRepository, never()).save(any());
    }

    @Test @DisplayName("getCategoryById() - Should return category when found")
    void getCategoryById_Found() {
        when(categoryRepository.findByCategoryId(1)).thenReturn(Optional.of(sampleCategory));
        Category result = categoryService.getCategoryById(1);
        assertEquals(1, result.getCategoryId());
        assertEquals("Food", result.getName());
    }

    @Test @DisplayName("getCategoryById() - Should throw when not found")
    void getCategoryById_NotFound_Throws() {
        when(categoryRepository.findByCategoryId(999)).thenReturn(Optional.empty());
        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> categoryService.getCategoryById(999));
        assertEquals("Category not found", ex.getMessage());
    }

    @Test @DisplayName("getByUserId() - Should return all categories for user")
    void getByUserId_ReturnsAll() {
        when(categoryRepository.findByUserId(10)).thenReturn(Arrays.asList(sampleCategory, sampleCategory));
        assertEquals(2, categoryService.getByUserId(10).size());
    }

    @Test @DisplayName("getByUserId() - Should return empty list when no categories")
    void getByUserId_Empty() {
        when(categoryRepository.findByUserId(10)).thenReturn(Collections.emptyList());
        assertTrue(categoryService.getByUserId(10).isEmpty());
    }

    @Test @DisplayName("getByUserAndType() - Should filter by CategoryType")
    void getByUserAndType_ReturnsFiltered() {
        when(categoryRepository.findByUserIdAndType(10, CategoryType.EXPENSE))
                .thenReturn(List.of(sampleCategory));
        List<Category> result = categoryService.getByUserAndType(10, CategoryType.EXPENSE);
        assertEquals(1, result.size());
        assertEquals(CategoryType.EXPENSE, result.get(0).getType());
    }

    @Test @DisplayName("updateCategory() - Should update non-null fields and save")
    void updateCategory_UpdatesFields() {
        CategoryRequest req = new CategoryRequest();
        req.setName("Food & Dining"); req.setIcon("🍽️"); req.setColorCode("#FF0000");
        req.setBudgetLimit(8000.0);
        when(categoryRepository.findByCategoryId(1)).thenReturn(Optional.of(sampleCategory));
        when(categoryRepository.save(any(Category.class))).thenReturn(sampleCategory);
        categoryService.updateCategory(1, req);
        assertEquals("Food & Dining", sampleCategory.getName());
        assertEquals("🍽️", sampleCategory.getIcon());
        assertEquals(8000.0, sampleCategory.getBudgetLimit());
        verify(categoryRepository).save(sampleCategory);
    }

    @Test @DisplayName("deleteCategory() - Should call deleteByCategoryId")
    void deleteCategory_CallsRepository() {
        doNothing().when(categoryRepository).deleteByCategoryId(1);
        categoryService.deleteCategory(1);
        verify(categoryRepository, times(1)).deleteByCategoryId(1);
    }

    @Test @DisplayName("getDefaultCategories() - Should return only default categories")
    void getDefaultCategories_ReturnsDefaults() {
        sampleCategory.setDefault(true);
        when(categoryRepository.findByIsDefault(true)).thenReturn(List.of(sampleCategory));
        List<Category> result = categoryService.getDefaultCategories();
        assertEquals(1, result.size());
        assertTrue(result.get(0).isDefault());
    }

    @Test @DisplayName("initDefaultCategories() - Should save 10 default categories")
    void initDefaultCategories_SavesTenCategories() {
        //when(categoryRepository.findByUserIdAndName(anyInt(), anyString())).thenReturn(Optional.empty());
        when(categoryRepository.save(any(Category.class))).thenReturn(sampleCategory);
        categoryService.initDefaultCategories(10);
        // 6 expense + 4 income = 10 default categories
        verify(categoryRepository, times(10)).save(any(Category.class));
    }

    @Test @DisplayName("setCategoryBudget() - Should update budget limit and save")
    void setCategoryBudget_UpdatesAndSaves() {
        when(categoryRepository.findByCategoryId(1)).thenReturn(Optional.of(sampleCategory));
        when(categoryRepository.save(any(Category.class))).thenReturn(sampleCategory);
        categoryService.setCategoryBudget(1, 10000.0);
        assertEquals(10000.0, sampleCategory.getBudgetLimit());
        verify(categoryRepository).save(sampleCategory);
    }

    @Test @DisplayName("getCategoryCount() - Should return correct count from repository")
    void getCategoryCount_ReturnsCount() {
        when(categoryRepository.countByUserId(10)).thenReturn(5L);
        assertEquals(5L, categoryService.getCategoryCount(10));
    }
}