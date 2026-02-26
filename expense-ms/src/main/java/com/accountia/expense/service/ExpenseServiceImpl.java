package com.accountia.expense.service;

import com.accountia.expense.domain.Expense;
import com.accountia.expense.domain.ExpenseCategory;
import com.accountia.expense.domain.ExpenseStatus;
import com.accountia.expense.dto.request.CreateExpenseRequest;
import com.accountia.expense.dto.request.RejectExpenseRequest;
import com.accountia.expense.dto.request.UpdateExpenseRequest;
import com.accountia.expense.dto.response.CategoryBreakdownResponse;
import com.accountia.expense.dto.response.ExpenseResponse;
import com.accountia.expense.dto.response.ExpenseSummaryResponse;
import com.accountia.expense.dto.response.MonthlyReportResponse;
import com.accountia.expense.exception.BusinessValidationException;
import com.accountia.expense.exception.ExpenseNotFoundException;
import com.accountia.expense.exception.InvalidStatusTransitionException;
import com.accountia.expense.feign.BusinessResponse;
import com.accountia.expense.feign.BusinessServiceClient;
import com.accountia.expense.mapper.ExpenseMapper;
import com.accountia.expense.messaging.EventPayloads;
import com.accountia.expense.messaging.EventPublisher;
import com.accountia.expense.repository.ExpenseCategoryRepository;
import com.accountia.expense.repository.ExpenseRepository;
import com.accountia.expense.repository.ExpenseSpecifications;
import feign.FeignException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@Transactional
public class ExpenseServiceImpl implements ExpenseService {

    private static final Set<String> ALLOWED_CURRENCIES = Set.of("TND", "EUR", "USD", "GBP");

    private final ExpenseRepository expenseRepository;
    private final ExpenseCategoryRepository categoryRepository;
    private final ExpenseMapper expenseMapper;
    private final BusinessServiceClient businessServiceClient;
    private final EventPublisher eventPublisher;
    private final BudgetService budgetService;
    private final StorageService storageService;

    public ExpenseServiceImpl(ExpenseRepository expenseRepository,
                              ExpenseCategoryRepository categoryRepository,
                              ExpenseMapper expenseMapper,
                              BusinessServiceClient businessServiceClient,
                              EventPublisher eventPublisher,
                              BudgetService budgetService,
                              StorageService storageService) {
        this.expenseRepository = expenseRepository;
        this.categoryRepository = categoryRepository;
        this.expenseMapper = expenseMapper;
        this.businessServiceClient = businessServiceClient;
        this.eventPublisher = eventPublisher;
        this.budgetService = budgetService;
        this.storageService = storageService;
    }

    @Override
    public ExpenseResponse createExpense(CreateExpenseRequest request) {
        validateCurrency(request.getCurrency());
        BusinessResponse business;
        try {
            business = businessServiceClient.getBusiness(request.getBusinessId());
        } catch (FeignException.NotFound ex) {
            throw new BusinessValidationException("Business not found: " + request.getBusinessId());
        } catch (FeignException ex) {
            throw new BusinessValidationException("Failed to validate business");
        }
        if (business == null || business.getStatus() == null || !"ACTIVE".equalsIgnoreCase(business.getStatus())) {
            throw new BusinessValidationException("Business is not active");
        }

        ExpenseCategory category = categoryRepository.findById(request.getCategoryId())
            .orElseThrow(() -> new BusinessValidationException("Category not found: " + request.getCategoryId()));
        if (!Objects.equals(category.getBusinessId(), request.getBusinessId())) {
            throw new BusinessValidationException("Category does not belong to business");
        }

        Expense expense = expenseMapper.toEntity(request);
        expense.setCurrency(request.getCurrency().toUpperCase(Locale.ROOT));
        expense.setStatus(ExpenseStatus.PENDING);
        expense.setTotalAmount(calculateTotal(expense.getAmount(), expense.getTaxAmount()));
        Expense saved = expenseRepository.save(expense);

        eventPublisher.publishExpenseSubmitted(
            new EventPayloads.ExpenseSubmittedEvent(
                saved.getId(),
                saved.getBusinessId(),
                saved.getSubmittedBy(),
                saved.getAmount(),
                saved.getCategoryId()
            )
        );

        budgetService.checkAndPublishBudgetExceeded(saved.getCategoryId(), saved.getBusinessId(), saved.getDate());
        return expenseMapper.toResponse(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public ExpenseResponse getExpense(Long id) {
        Expense expense = expenseRepository.findByIdAndDeletedAtIsNull(id)
            .orElseThrow(() -> new ExpenseNotFoundException(id));
        return expenseMapper.toResponse(expense);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<ExpenseResponse> listExpenses(Long businessId,
                                              String status,
                                              Long categoryId,
                                              LocalDate startDate,
                                              LocalDate endDate,
                                              String vendor,
                                              Pageable pageable) {
        ExpenseStatus expenseStatus = null;
        if (status != null && !status.isBlank()) {
            try {
                expenseStatus = ExpenseStatus.valueOf(status.toUpperCase(Locale.ROOT));
            } catch (IllegalArgumentException ex) {
                throw new BusinessValidationException("Invalid status: " + status);
            }
        }
        Specification<Expense> spec = Specification.where(ExpenseSpecifications.hasBusinessId(businessId))
            .and(ExpenseSpecifications.hasStatus(expenseStatus))
            .and(ExpenseSpecifications.hasCategoryId(categoryId))
            .and(ExpenseSpecifications.dateOnOrAfter(startDate))
            .and(ExpenseSpecifications.dateOnOrBefore(endDate))
            .and(ExpenseSpecifications.hasVendor(vendor));

        return expenseRepository.findAll(spec, pageable)
            .map(expenseMapper::toResponse);
    }

    @Override
    public ExpenseResponse updateExpense(Long id, UpdateExpenseRequest request) {
        Expense expense = expenseRepository.findByIdAndDeletedAtIsNull(id)
            .orElseThrow(() -> new ExpenseNotFoundException(id));
        if (expense.getStatus() != ExpenseStatus.PENDING) {
            throw new InvalidStatusTransitionException("Only PENDING expenses can be updated");
        }

        if (request.getCategoryId() != null) {
            ExpenseCategory category = categoryRepository.findById(request.getCategoryId())
                .orElseThrow(() -> new BusinessValidationException("Category not found: " + request.getCategoryId()));
            if (!Objects.equals(category.getBusinessId(), expense.getBusinessId())) {
                throw new BusinessValidationException("Category does not belong to business");
            }
        }
        if (request.getCurrency() != null) {
            validateCurrency(request.getCurrency());
        }

        expenseMapper.updateEntity(request, expense);
        if (request.getCurrency() != null) {
            expense.setCurrency(request.getCurrency().toUpperCase(Locale.ROOT));
        }
        expense.setTotalAmount(calculateTotal(expense.getAmount(), expense.getTaxAmount()));
        return expenseMapper.toResponse(expenseRepository.save(expense));
    }

    @Override
    public void deleteExpense(Long id) {
        Expense expense = expenseRepository.findByIdAndDeletedAtIsNull(id)
            .orElseThrow(() -> new ExpenseNotFoundException(id));
        if (expense.getStatus() != ExpenseStatus.PENDING) {
            throw new InvalidStatusTransitionException("Only PENDING expenses can be deleted");
        }
        expense.setDeletedAt(LocalDateTime.now());
        expenseRepository.save(expense);
        eventPublisher.publishExpenseDeleted(
            new EventPayloads.ExpenseDeletedEvent(expense.getId(), expense.getBusinessId())
        );
    }

    @Override
    public ExpenseResponse approveExpense(Long id, Long approvedBy) {
        Expense expense = expenseRepository.findByIdAndDeletedAtIsNull(id)
            .orElseThrow(() -> new ExpenseNotFoundException(id));
        if (expense.getStatus() != ExpenseStatus.PENDING) {
            throw new InvalidStatusTransitionException("Only PENDING expenses can be approved");
        }
        if (approvedBy == null) {
            throw new BusinessValidationException("approvedBy is required");
        }
        if (Objects.equals(expense.getSubmittedBy(), approvedBy)) {
            throw new BusinessValidationException("Self-approval is not allowed");
        }
        expense.setStatus(ExpenseStatus.APPROVED);
        expense.setApprovedBy(approvedBy);
        expense.setApprovedAt(LocalDateTime.now());
        Expense saved = expenseRepository.save(expense);
        eventPublisher.publishExpenseApproved(
            new EventPayloads.ExpenseApprovedEvent(saved.getId(), saved.getBusinessId(), approvedBy, saved.getAmount())
        );
        budgetService.checkAndPublishBudgetExceeded(saved.getCategoryId(), saved.getBusinessId(), saved.getDate());
        return expenseMapper.toResponse(saved);
    }

    @Override
    public ExpenseResponse rejectExpense(Long id, Long approvedBy, RejectExpenseRequest request) {
        Expense expense = expenseRepository.findByIdAndDeletedAtIsNull(id)
            .orElseThrow(() -> new ExpenseNotFoundException(id));
        if (expense.getStatus() != ExpenseStatus.PENDING) {
            throw new InvalidStatusTransitionException("Only PENDING expenses can be rejected");
        }
        if (approvedBy == null) {
            throw new BusinessValidationException("approvedBy is required");
        }
        if (Objects.equals(expense.getSubmittedBy(), approvedBy)) {
            throw new BusinessValidationException("Self-approval is not allowed");
        }
        expense.setStatus(ExpenseStatus.REJECTED);
        expense.setApprovedBy(approvedBy);
        expense.setApprovedAt(LocalDateTime.now());
        expense.setRejectionReason(request.getRejectionReason());
        Expense saved = expenseRepository.save(expense);
        eventPublisher.publishExpenseRejected(
            new EventPayloads.ExpenseRejectedEvent(saved.getId(), saved.getBusinessId(), approvedBy, request.getRejectionReason())
        );
        return expenseMapper.toResponse(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public ExpenseSummaryResponse getSummary(Long businessId) {
        if (businessId == null) {
            throw new BusinessValidationException("businessId is required");
        }
        Map<String, Double> totalsByStatus = new HashMap<>();
        for (Object[] row : expenseRepository.sumTotalsByStatus(businessId)) {
            totalsByStatus.put(String.valueOf(row[0]), toDouble(row[1]));
        }
        Map<Long, Double> totalsByCategory = new HashMap<>();
        for (Object[] row : expenseRepository.sumTotalsByCategory(businessId)) {
            totalsByCategory.put((Long) row[0], toDouble(row[1]));
        }
        double totalAmount = totalsByStatus.values().stream().mapToDouble(Double::doubleValue).sum();
        return ExpenseSummaryResponse.builder()
            .totalAmount(totalAmount)
            .totalsByStatus(totalsByStatus)
            .totalsByCategory(totalsByCategory)
            .build();
    }

    @Override
    @Transactional(readOnly = true)
    public List<CategoryBreakdownResponse> getCategoryBreakdown(Long businessId) {
        if (businessId == null) {
            throw new BusinessValidationException("businessId is required");
        }
        LocalDate now = LocalDate.now();
        List<ExpenseCategory> categories = categoryRepository.findByBusinessId(businessId);
        return categories.stream().map(category -> {
            Double total = expenseRepository.sumCategoryMonthTotals(
                category.getId(),
                List.of(ExpenseStatus.PENDING, ExpenseStatus.APPROVED),
                now.getYear(),
                now.getMonthValue()
            );
            double currentTotal = total == null ? 0.0 : total;
            boolean exceeded = category.getBudget() != null && currentTotal > category.getBudget();
            return CategoryBreakdownResponse.builder()
                .categoryId(category.getId())
                .categoryName(category.getName())
                .budget(category.getBudget())
                .currentTotal(currentTotal)
                .budgetExceeded(exceeded)
                .build();
        }).collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public MonthlyReportResponse getMonthlyReport(Long businessId, int year, int month) {
        if (businessId == null) {
            throw new BusinessValidationException("businessId is required");
        }
        Map<String, Double> totalsByStatus = new HashMap<>();
        for (Object[] row : expenseRepository.sumMonthTotalsByStatus(businessId, year, month)) {
            totalsByStatus.put(String.valueOf(row[0]), toDouble(row[1]));
        }
        Map<Long, Double> totalsByCategory = new HashMap<>();
        for (Object[] row : expenseRepository.sumMonthTotalsByCategory(businessId, year, month)) {
            totalsByCategory.put((Long) row[0], toDouble(row[1]));
        }
        List<CategoryBreakdownResponse> breakdown = categoryRepository.findByBusinessId(businessId).stream()
            .map(category -> {
                double currentTotal = totalsByCategory.getOrDefault(category.getId(), 0.0);
                boolean exceeded = category.getBudget() != null && currentTotal > category.getBudget();
                return CategoryBreakdownResponse.builder()
                    .categoryId(category.getId())
                    .categoryName(category.getName())
                    .budget(category.getBudget())
                    .currentTotal(currentTotal)
                    .budgetExceeded(exceeded)
                    .build();
            })
            .collect(Collectors.toList());

        Double totalAmount = expenseRepository.sumBusinessMonthTotal(businessId, year, month);
        return MonthlyReportResponse.builder()
            .year(year)
            .month(month)
            .totalAmount(totalAmount == null ? 0.0 : totalAmount)
            .totalsByStatus(totalsByStatus)
            .categoryBreakdown(breakdown)
            .build();
    }

    @Override
    public ExpenseResponse uploadReceipt(Long id, MultipartFile file) {
        Expense expense = expenseRepository.findByIdAndDeletedAtIsNull(id)
            .orElseThrow(() -> new ExpenseNotFoundException(id));
        String receiptUrl = storageService.storeReceipt(expense.getBusinessId(), expense.getId(), file);
        expense.setReceiptUrl(receiptUrl);
        return expenseMapper.toResponse(expenseRepository.save(expense));
    }

    @Override
    public void softDeleteByBusiness(Long businessId) {
        expenseRepository.softDeleteByBusinessId(businessId, LocalDateTime.now());
    }

    private void validateCurrency(String currency) {
        if (currency == null || currency.isBlank()) {
            throw new BusinessValidationException("Currency is required");
        }
        String normalized = currency.toUpperCase(Locale.ROOT);
        if (!ALLOWED_CURRENCIES.contains(normalized)) {
            throw new BusinessValidationException("Unsupported currency: " + currency);
        }
    }

    private double calculateTotal(Double amount, Double taxAmount) {
        double base = amount == null ? 0.0 : amount;
        double tax = taxAmount == null ? 0.0 : taxAmount;
        return base + tax;
    }

    private double toDouble(Object value) {
        if (value == null) {
            return 0.0;
        }
        return ((Number) value).doubleValue();
    }
}
