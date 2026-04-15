package com.accountia.expense.controller;

import com.accountia.expense.dto.ExpenseRequest;
import com.accountia.expense.dto.ExpenseResponse;
import com.accountia.expense.service.ExpenseService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/expense")
@RequiredArgsConstructor
public class ExpenseController {

    private final ExpenseService expenseService;

    @GetMapping("/health")
    public ResponseEntity<?> health() {
        return ResponseEntity.ok("expense-ms up");
    }

    @PostMapping
    public ResponseEntity<ExpenseResponse> createExpense(@RequestBody ExpenseRequest request) {
        return new ResponseEntity<>(expenseService.createExpense(request), HttpStatus.CREATED);
    }

    @GetMapping("/business/{businessId}")
    public ResponseEntity<List<ExpenseResponse>> getExpensesByBusiness(@PathVariable String businessId) {
        return ResponseEntity.ok(expenseService.getExpensesByBusiness(businessId));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteExpense(@PathVariable String id) {
        expenseService.deleteExpense(id);
        return ResponseEntity.noContent().build();
    }
}
