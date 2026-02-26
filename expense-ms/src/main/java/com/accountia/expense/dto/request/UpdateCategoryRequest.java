package com.accountia.expense.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Positive;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UpdateCategoryRequest {

    @Schema(example = "Operations")
    private String name;

    @Schema(example = "#00AAFF")
    private String color;

    @Schema(example = "wrench")
    private String icon;

    @Positive
    @Schema(example = "2500.0")
    private Double budget;
}
