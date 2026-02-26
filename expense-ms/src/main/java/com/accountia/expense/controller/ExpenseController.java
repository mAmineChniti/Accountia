package com.accountia.expense.controller;

import com.accountia.expense.dto.request.CreateExpenseRequest;
import com.accountia.expense.dto.request.RejectExpenseRequest;
import com.accountia.expense.dto.request.UpdateExpenseRequest;
import com.accountia.expense.dto.response.CategoryBreakdownResponse;
import com.accountia.expense.dto.response.ExpenseResponse;
import com.accountia.expense.dto.response.ExpenseSummaryResponse;
import com.accountia.expense.dto.response.MonthlyReportResponse;
import com.accountia.expense.service.ExpenseService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/expenses")
public class ExpenseController {

    private final ExpenseService expenseService;

    public ExpenseController(ExpenseService expenseService) {
        this.expenseService = expenseService;
    }

    @Operation(summary = "Create expense")
    @PostMapping
    public ResponseEntity<ExpenseResponse> createExpense(@Valid @RequestBody CreateExpenseRequest request) {
        return ResponseEntity.ok(expenseService.createExpense(request));
    }

    @Operation(summary = "List expenses (paginated + filtered)")
    @GetMapping
    public ResponseEntity<Page<ExpenseResponse>> listExpenses(
        @RequestParam(required = false) Long businessId,
        @RequestParam(required = false) String status,
        @RequestParam(required = false) Long categoryId,
        @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
        @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
        @RequestParam(required = false) String vendor,
        Pageable pageable
    ) {
        return ResponseEntity.ok(expenseService.listExpenses(
            businessId, status, categoryId, startDate, endDate, vendor, pageable));
    }

    @Operation(summary = "Get expense by ID")
    @GetMapping("/{id}")
    public ResponseEntity<ExpenseResponse> getExpense(@PathVariable Long id) {
        return ResponseEntity.ok(expenseService.getExpense(id));
    }

    @Operation(summary = "Update expense (only if status = PENDING)")
    @PutMapping("/{id}")
    public ResponseEntity<ExpenseResponse> updateExpense(@PathVariable Long id,
                                                         @Valid @RequestBody UpdateExpenseRequest request) {
        return ResponseEntity.ok(expenseService.updateExpense(id, request));
    }

    @Operation(summary = "Soft-delete expense (only if status = PENDING)")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteExpense(@PathVariable Long id) {
        expenseService.deleteExpense(id);
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "Approve expense")
    @PatchMapping("/{id}/approve")
    public ResponseEntity<ExpenseResponse> approveExpense(@PathVariable Long id,
                                                          @Parameter(description = "Approver user ID")
                                                          @RequestParam Long approvedBy) {
        return ResponseEntity.ok(expenseService.approveExpense(id, approvedBy));
    }

    @Operation(summary = "Reject expense")
    @PatchMapping("/{id}/reject")
    public ResponseEntity<ExpenseResponse> rejectExpense(@PathVariable Long id,
                                                         @Parameter(description = "Approver user ID")
                                                         @RequestParam Long approvedBy,
                                                         @Valid @RequestBody RejectExpenseRequest request) {
        return ResponseEntity.ok(expenseService.rejectExpense(id, approvedBy, request));
    }

    @Operation(summary = "Expense summary totals")
    @GetMapping("/summary")
    public ResponseEntity<ExpenseSummaryResponse> getSummary(@RequestParam Long businessId) {
        return ResponseEntity.ok(expenseService.getSummary(businessId));
    }

    @Operation(summary = "Breakdown by category with budget comparison")
    @GetMapping("/by-category")
    public ResponseEntity<List<CategoryBreakdownResponse>> getByCategory(@RequestParam Long businessId) {
        return ResponseEntity.ok(expenseService.getCategoryBreakdown(businessId));
    }

    @Operation(summary = "Monthly expense report")
    @GetMapping("/monthly-report")
    public ResponseEntity<MonthlyReportResponse> getMonthlyReport(@RequestParam Long businessId,
                                                                  @RequestParam int year,
                                                                  @RequestParam int month) {
        return ResponseEntity.ok(expenseService.getMonthlyReport(businessId, year, month));
    }

    @Operation(summary = "Upload receipt")
    @PostMapping(value = "/{id}/receipt", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ExpenseResponse> uploadReceipt(@PathVariable Long id,
                                                         @RequestPart("file") MultipartFile file) {
        return ResponseEntity.ok(expenseService.uploadReceipt(id, file));
    }
}
