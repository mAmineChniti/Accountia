package com.accountia.expense.dto.request;

import com.accountia.expense.domain.PaymentMethod;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Positive;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
public class CreateExpenseRequest {

    @NotNull
    @Schema(example = "1001")
    private Long businessId;

    @NotNull
    @Schema(example = "501")
    private Long categoryId;

    @NotBlank
    @Schema(example = "Cloud hosting for February")
    private String description;

    @NotNull
    @Positive
    @Schema(example = "120.5")
    private Double amount;

    @Positive
    @Schema(example = "24.1")
    private Double taxAmount;

    @NotBlank
    @Pattern(regexp = "TND|EUR|USD|GBP")
    @Schema(example = "USD")
    private String currency;

    @NotNull
    @Schema(example = "2026-02-15")
    private LocalDate date;

    @Schema(example = "Vendor Inc.")
    private String vendor;

    private PaymentMethod paymentMethod;

    @Schema(example = "infra,cloud,feb")
    private String tags;

    @NotNull
    @Schema(example = "9001")
    private Long submittedBy;
}
