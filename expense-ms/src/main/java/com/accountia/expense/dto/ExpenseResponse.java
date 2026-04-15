package com.accountia.expense.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ExpenseResponse {
    private String id;
    private String businessId;
    private String businessName;
    private String category;
    private BigDecimal amount;
    private String description;
    private LocalDate date;
    private LocalDateTime createdAt;
}
