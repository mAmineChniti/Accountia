package com.accountia.expense.repository;

import com.accountia.expense.domain.Expense;
import com.accountia.expense.domain.ExpenseStatus;
import org.springframework.data.jpa.domain.Specification;

import java.time.LocalDate;

public final class ExpenseSpecifications {

    private ExpenseSpecifications() {
    }

    public static Specification<Expense> hasBusinessId(Long businessId) {
        return (root, query, cb) -> businessId == null ? null : cb.equal(root.get("businessId"), businessId);
    }

    public static Specification<Expense> hasStatus(ExpenseStatus status) {
        return (root, query, cb) -> status == null ? null : cb.equal(root.get("status"), status);
    }

    public static Specification<Expense> hasCategoryId(Long categoryId) {
        return (root, query, cb) -> categoryId == null ? null : cb.equal(root.get("categoryId"), categoryId);
    }

    public static Specification<Expense> hasVendor(String vendor) {
        return (root, query, cb) -> vendor == null || vendor.isBlank()
            ? null
            : cb.like(cb.lower(root.get("vendor")), "%" + vendor.toLowerCase() + "%");
    }

    public static Specification<Expense> dateOnOrAfter(LocalDate startDate) {
        return (root, query, cb) -> startDate == null ? null : cb.greaterThanOrEqualTo(root.get("date"), startDate);
    }

    public static Specification<Expense> dateOnOrBefore(LocalDate endDate) {
        return (root, query, cb) -> endDate == null ? null : cb.lessThanOrEqualTo(root.get("date"), endDate);
    }
}
