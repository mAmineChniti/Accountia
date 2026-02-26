package com.accountia.expense.repository;

import com.accountia.expense.domain.Expense;
import com.accountia.expense.domain.ExpenseStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface ExpenseRepository extends JpaRepository<Expense, Long>, JpaSpecificationExecutor<Expense> {

    Optional<Expense> findByIdAndDeletedAtIsNull(Long id);

    @Query("select e.status, sum(e.totalAmount) from Expense e " +
        "where e.deletedAt is null and e.businessId = :businessId group by e.status")
    List<Object[]> sumTotalsByStatus(@Param("businessId") Long businessId);

    @Query("select e.categoryId, sum(e.totalAmount) from Expense e " +
        "where e.deletedAt is null and e.businessId = :businessId group by e.categoryId")
    List<Object[]> sumTotalsByCategory(@Param("businessId") Long businessId);

    @Query("select sum(e.totalAmount) from Expense e " +
        "where e.deletedAt is null and e.categoryId = :categoryId " +
        "and e.status in :statuses " +
        "and YEAR(e.date) = :year and MONTH(e.date) = :month")
    Double sumCategoryMonthTotals(
        @Param("categoryId") Long categoryId,
        @Param("statuses") List<ExpenseStatus> statuses,
        @Param("year") int year,
        @Param("month") int month
    );

    @Query("select sum(e.totalAmount) from Expense e " +
        "where e.deletedAt is null and e.businessId = :businessId " +
        "and YEAR(e.date) = :year and MONTH(e.date) = :month")
    Double sumBusinessMonthTotal(
        @Param("businessId") Long businessId,
        @Param("year") int year,
        @Param("month") int month
    );

    @Query("select e.status, sum(e.totalAmount) from Expense e " +
        "where e.deletedAt is null and e.businessId = :businessId " +
        "and YEAR(e.date) = :year and MONTH(e.date) = :month " +
        "group by e.status")
    List<Object[]> sumMonthTotalsByStatus(
        @Param("businessId") Long businessId,
        @Param("year") int year,
        @Param("month") int month
    );

    @Query("select e.categoryId, sum(e.totalAmount) from Expense e " +
        "where e.deletedAt is null and e.businessId = :businessId " +
        "and YEAR(e.date) = :year and MONTH(e.date) = :month " +
        "group by e.categoryId")
    List<Object[]> sumMonthTotalsByCategory(
        @Param("businessId") Long businessId,
        @Param("year") int year,
        @Param("month") int month
    );

    @Transactional
    @Modifying
    @Query("update Expense e set e.deletedAt = :deletedAt where e.businessId = :businessId and e.deletedAt is null")
    int softDeleteByBusinessId(@Param("businessId") Long businessId, @Param("deletedAt") LocalDateTime deletedAt);
}
