package com.accountia.expense.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
public class CategoryResponse {

    @Schema(example = "501")
    private Long id;

    @Schema(example = "1001")
    private Long businessId;

    @Schema(example = "Marketing")
    private String name;

    @Schema(example = "#FFAA00")
    private String color;

    @Schema(example = "bullhorn")
    private String icon;

    @Schema(example = "1500.0")
    private Double budget;

    private LocalDateTime createdAt;
}
