package com.accountia.expense.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class RejectExpenseRequest {

    @NotBlank
    @Schema(example = "Receipt missing required details.")
    private String rejectionReason;
}
