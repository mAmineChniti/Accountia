package com.accountia.expense.dto.response;

import com.accountia.expense.domain.ExpenseStatus;
import com.accountia.expense.domain.PaymentMethod;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Getter
@Builder
public class ExpenseResponse {

    @Schema(example = "2001")
    private Long id;

    @Schema(example = "1001")
    private Long businessId;

    @Schema(example = "501")
    private Long categoryId;

    @Schema(example = "Cloud hosting for February")
    private String description;

    @Schema(example = "120.5")
    private Double amount;

    @Schema(example = "24.1")
    private Double taxAmount;

    @Schema(example = "144.6")
    private Double totalAmount;

    @Schema(example = "USD")
    private String currency;

    @Schema(example = "2026-02-15")
    private LocalDate date;

    @Schema(example = "/uploads/receipts/1001/receipt-2001.pdf")
    private String receiptUrl;

    private ExpenseStatus status;

    private Long approvedBy;

    private LocalDateTime approvedAt;

    private String rejectionReason;

    private String vendor;

    private PaymentMethod paymentMethod;

    private String tags;

    private Long submittedBy;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;
}
