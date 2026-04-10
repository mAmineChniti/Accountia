package com.accountia.expense.repository;

import com.accountia.expense.entity.Expense;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface ExpenseRepository extends JpaRepository<Expense, Long> {
    List<Expense> findByCategorieIgnoreCase(String categorie);
    List<Expense> findByCategorieIgnoreCaseAndOwnerSubject(String categorie, String ownerSubject);
    List<Expense> findByDateDepenseBetween(LocalDate startDate, LocalDate endDate);
    List<Expense> findByOwnerSubject(String ownerSubject);
    Optional<Expense> findByIdAndOwnerSubject(Long id, String ownerSubject);
}
