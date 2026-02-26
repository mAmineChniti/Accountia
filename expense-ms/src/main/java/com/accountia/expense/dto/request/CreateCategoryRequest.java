package com.accountia.expense.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CreateCategoryRequest {

    @NotNull
    @Schema(example = "1001")
    private Long businessId;

    @NotBlank
    @Schema(example = "Marketing")
    private String name;

    @Schema(example = "#FFAA00")
    private String color;

    @Schema(example = "bullhorn")
    private String icon;

    @Positive
    @Schema(example = "1500.0")
    private Double budget;
}
