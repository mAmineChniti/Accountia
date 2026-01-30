package com.accountia.invoice.dto;

import lombok.Data;

import java.time.LocalDate;

@Data
public class InvoiceDTO {
    private Long id;
    private String tenantId;
    private String clientName;
    private Double amount;
    private LocalDate issueDate;
}
