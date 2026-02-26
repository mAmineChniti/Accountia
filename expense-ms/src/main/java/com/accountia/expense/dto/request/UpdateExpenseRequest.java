package com.accountia.expense.dto.request;

import com.accountia.expense.domain.PaymentMethod;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Positive;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
public class UpdateExpenseRequest {

    @Schema(example = "502")
    private Long categoryId;

    @Schema(example = "Updated description")
    private String description;

    @Positive
    @Schema(example = "95.0")
    private Double amount;

    @Positive
    @Schema(example = "5.0")
    private Double taxAmount;

    @Pattern(regexp = "TND|EUR|USD|GBP")
    @Schema(example = "EUR")
    private String currency;

    @Schema(example = "2026-02-10")
    private LocalDate date;

    @Schema(example = "Updated vendor")
    private String vendor;

    private PaymentMethod paymentMethod;

    @Schema(example = "ops,recurring")
    private String tags;
}
