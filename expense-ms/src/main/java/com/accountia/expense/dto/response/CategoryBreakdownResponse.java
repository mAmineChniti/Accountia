package com.accountia.expense.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class CategoryBreakdownResponse {

    @Schema(example = "501")
    private Long categoryId;

    @Schema(example = "Marketing")
    private String categoryName;

    @Schema(example = "1500.0")
    private Double budget;

    @Schema(example = "1750.0")
    private Double currentTotal;

    @Schema(example = "true")
    private boolean budgetExceeded;
}
