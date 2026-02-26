package com.accountia.expense.service;

import java.time.LocalDate;

public interface BudgetService {

    void checkAndPublishBudgetExceeded(Long categoryId, Long businessId, LocalDate expenseDate);
}
