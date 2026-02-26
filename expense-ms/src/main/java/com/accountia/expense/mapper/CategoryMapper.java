package com.accountia.expense.mapper;

import com.accountia.expense.domain.ExpenseCategory;
import com.accountia.expense.dto.request.CreateCategoryRequest;
import com.accountia.expense.dto.request.UpdateCategoryRequest;
import com.accountia.expense.dto.response.CategoryResponse;
import org.mapstruct.*;

@Mapper(componentModel = "spring")
public interface CategoryMapper {

    CategoryResponse toResponse(ExpenseCategory category);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    ExpenseCategory toEntity(CreateCategoryRequest request);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "businessId", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    void updateEntity(UpdateCategoryRequest request, @MappingTarget ExpenseCategory category);
}
