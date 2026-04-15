package com.accountia.expense.service;

import com.accountia.expense.client.BusinessClient;
import com.accountia.expense.dto.BusinessSummaryDTO;
import com.accountia.expense.dto.ExpenseRequest;
import com.accountia.expense.dto.ExpenseResponse;
import com.accountia.expense.messaging.ExpenseEventPublisher;
import com.accountia.expense.model.Expense;
import com.accountia.expense.repository.ExpenseRepository;
import com.accountia.expense_ms.exception.ResourceNotFoundException;
import feign.FeignException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class ExpenseService {

    private final ExpenseRepository expenseRepository;
    private final BusinessClient businessClient;
    private final ExpenseEventPublisher eventPublisher;

    @Transactional
    public ExpenseResponse createExpense(ExpenseRequest request) {
        log.info("Creating expense for business: {}", request.getBusinessId());
        
        // Validate business via Feign
        BusinessSummaryDTO business = validateBusiness(request.getBusinessId());

        Expense expense = Expense.builder()
                .businessId(request.getBusinessId())
                .category(request.getCategory())
                .amount(request.getAmount())
                .description(request.getDescription())
                .date(request.getDate())
                .build();

        Expense savedExpense = expenseRepository.save(expense);
        
        // Publish event
        eventPublisher.publishExpenseCreated(savedExpense);

        return mapToResponse(savedExpense, business.getName());
    }

    @Transactional(readOnly = true)
    public List<ExpenseResponse> getExpensesByBusiness(String businessId) {
        BusinessSummaryDTO business = validateBusiness(businessId);
        return expenseRepository.findByBusinessId(businessId).stream()
                .map(e -> mapToResponse(e, business.getName()))
                .collect(Collectors.toList());
    }

    @Transactional
    public void deleteExpense(String id) {
        Expense expense = expenseRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Expense not found with id: " + id));
        
        expenseRepository.delete(expense);
        eventPublisher.publishExpenseDeleted(id, expense.getBusinessId());
    }

    private BusinessSummaryDTO validateBusiness(String businessId) {
        try {
            BusinessSummaryDTO business = businessClient.getBusinessSummary(businessId);
            if (business == null || !business.isActive()) {
                throw new IllegalStateException("Business is inactive or invalid");
            }
            return business;
        } catch (FeignException.NotFound e) {
            throw new ResourceNotFoundException("Business not found with id: " + businessId);
        } catch (Exception e) {
            log.error("Error validating business: {}", e.getMessage());
            throw new RuntimeException("Validation service unavailable");
        }
    }

    private ExpenseResponse mapToResponse(Expense expense, String businessName) {
        return ExpenseResponse.builder()
                .id(expense.getId())
                .businessId(expense.getBusinessId())
                .businessName(businessName)
                .category(expense.getCategory())
                .amount(expense.getAmount())
                .description(expense.getDescription())
                .date(expense.getDate())
                .createdAt(expense.getCreatedAt())
                .build();
    }
}
