package com.accountia.expense.service;

import com.accountia.expense.dto.request.CreateCategoryRequest;
import com.accountia.expense.dto.request.UpdateCategoryRequest;
import com.accountia.expense.dto.response.CategoryResponse;

import java.util.List;

public interface CategoryService {

    CategoryResponse createCategory(CreateCategoryRequest request);

    List<CategoryResponse> listCategories(Long businessId);

    CategoryResponse updateCategory(Long id, UpdateCategoryRequest request);

    void deleteCategory(Long id);
}
