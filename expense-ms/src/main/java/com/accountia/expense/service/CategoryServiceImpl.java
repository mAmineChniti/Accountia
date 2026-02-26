package com.accountia.expense.service;

import com.accountia.expense.domain.ExpenseCategory;
import com.accountia.expense.dto.request.CreateCategoryRequest;
import com.accountia.expense.dto.request.UpdateCategoryRequest;
import com.accountia.expense.dto.response.CategoryResponse;
import com.accountia.expense.exception.BusinessValidationException;
import com.accountia.expense.mapper.CategoryMapper;
import com.accountia.expense.repository.ExpenseCategoryRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
public class CategoryServiceImpl implements CategoryService {

    private final ExpenseCategoryRepository categoryRepository;
    private final CategoryMapper categoryMapper;

    public CategoryServiceImpl(ExpenseCategoryRepository categoryRepository, CategoryMapper categoryMapper) {
        this.categoryRepository = categoryRepository;
        this.categoryMapper = categoryMapper;
    }

    @Override
    public CategoryResponse createCategory(CreateCategoryRequest request) {
        ExpenseCategory category = categoryMapper.toEntity(request);
        ExpenseCategory saved = categoryRepository.save(category);
        return categoryMapper.toResponse(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public List<CategoryResponse> listCategories(Long businessId) {
        if (businessId == null) {
            throw new BusinessValidationException("businessId is required");
        }
        return categoryRepository.findByBusinessId(businessId).stream()
            .map(categoryMapper::toResponse)
            .collect(Collectors.toList());
    }

    @Override
    public CategoryResponse updateCategory(Long id, UpdateCategoryRequest request) {
        ExpenseCategory category = categoryRepository.findById(id)
            .orElseThrow(() -> new BusinessValidationException("Category not found: " + id));
        categoryMapper.updateEntity(request, category);
        return categoryMapper.toResponse(categoryRepository.save(category));
    }

    @Override
    public void deleteCategory(Long id) {
        if (!categoryRepository.existsById(id)) {
            throw new BusinessValidationException("Category not found: " + id);
        }
        categoryRepository.deleteById(id);
    }
}
