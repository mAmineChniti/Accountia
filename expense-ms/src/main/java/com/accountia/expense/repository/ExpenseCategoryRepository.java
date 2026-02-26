package com.accountia.expense.repository;

import com.accountia.expense.domain.ExpenseCategory;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ExpenseCategoryRepository extends JpaRepository<ExpenseCategory, Long> {

    List<ExpenseCategory> findByBusinessId(Long businessId);
}
