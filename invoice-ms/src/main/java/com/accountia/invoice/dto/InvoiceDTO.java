package com.accountia.invoice.dto;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
public class InvoiceDTO {
    private Long id;
    private String invoiceNumber;
    private String tenantId;
    private String clientName;
    private String description;
    private BigDecimal amount;
    private BigDecimal taxAmount;
    private BigDecimal totalAmount;
    private LocalDate issueDate;
    private LocalDate dueDate;
    private String status;
    private Long businessId;
    private Long clientId;
}
