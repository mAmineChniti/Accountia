package com.accountia.expense.service;

import com.accountia.expense.domain.ExpenseCategory;
import com.accountia.expense.domain.ExpenseStatus;
import com.accountia.expense.messaging.EventPayloads;
import com.accountia.expense.messaging.EventPublisher;
import com.accountia.expense.repository.ExpenseCategoryRepository;
import com.accountia.expense.repository.ExpenseRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;

@Service
public class BudgetServiceImpl implements BudgetService {

    private final ExpenseRepository expenseRepository;
    private final ExpenseCategoryRepository categoryRepository;
    private final EventPublisher eventPublisher;

    public BudgetServiceImpl(ExpenseRepository expenseRepository,
                             ExpenseCategoryRepository categoryRepository,
                             EventPublisher eventPublisher) {
        this.expenseRepository = expenseRepository;
        this.categoryRepository = categoryRepository;
        this.eventPublisher = eventPublisher;
    }

    @Override
    public void checkAndPublishBudgetExceeded(Long categoryId, Long businessId, LocalDate expenseDate) {
        ExpenseCategory category = categoryRepository.findById(categoryId).orElse(null);
        if (category == null || category.getBudget() == null) {
            return;
        }
        Double total = expenseRepository.sumCategoryMonthTotals(
            categoryId,
            List.of(ExpenseStatus.PENDING, ExpenseStatus.APPROVED),
            expenseDate.getYear(),
            expenseDate.getMonthValue()
        );
        double currentTotal = total == null ? 0.0 : total;
        if (currentTotal > category.getBudget()) {
            eventPublisher.publishBudgetExceeded(
                new EventPayloads.BudgetExceededEvent(categoryId, businessId, currentTotal, category.getBudget())
            );
        }
    }
}
