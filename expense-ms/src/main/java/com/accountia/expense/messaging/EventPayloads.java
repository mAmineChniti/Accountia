package com.accountia.expense.messaging;

public final class EventPayloads {

    private EventPayloads() {
    }

    public record ExpenseSubmittedEvent(Long expenseId, Long businessId, Long submittedBy, Double amount, Long categoryId) {}

    public record ExpenseApprovedEvent(Long expenseId, Long businessId, Long approvedBy, Double amount) {}

    public record ExpenseRejectedEvent(Long expenseId, Long businessId, Long approvedBy, String rejectionReason) {}

    public record ExpenseDeletedEvent(Long expenseId, Long businessId) {}

    public record BudgetExceededEvent(Long categoryId, Long businessId, Double currentTotal, Double budget) {}

    public record BusinessDeletedEvent(Long businessId) {}
}
