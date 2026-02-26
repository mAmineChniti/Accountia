package com.accountia.expense.service;

import com.accountia.expense.dto.request.CreateExpenseRequest;
import com.accountia.expense.dto.request.RejectExpenseRequest;
import com.accountia.expense.dto.request.UpdateExpenseRequest;
import com.accountia.expense.dto.response.CategoryBreakdownResponse;
import com.accountia.expense.dto.response.ExpenseResponse;
import com.accountia.expense.dto.response.ExpenseSummaryResponse;
import com.accountia.expense.dto.response.MonthlyReportResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDate;
import java.util.List;

public interface ExpenseService {

    ExpenseResponse createExpense(CreateExpenseRequest request);

    ExpenseResponse getExpense(Long id);

    Page<ExpenseResponse> listExpenses(Long businessId,
                                      String status,
                                      Long categoryId,
                                      LocalDate startDate,
                                      LocalDate endDate,
                                      String vendor,
                                      Pageable pageable);

    ExpenseResponse updateExpense(Long id, UpdateExpenseRequest request);

    void deleteExpense(Long id);

    ExpenseResponse approveExpense(Long id, Long approvedBy);

    ExpenseResponse rejectExpense(Long id, Long approvedBy, RejectExpenseRequest request);

    ExpenseSummaryResponse getSummary(Long businessId);

    List<CategoryBreakdownResponse> getCategoryBreakdown(Long businessId);

    MonthlyReportResponse getMonthlyReport(Long businessId, int year, int month);

    ExpenseResponse uploadReceipt(Long id, MultipartFile file);

    void softDeleteByBusiness(Long businessId);
}
