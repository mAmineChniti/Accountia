package com.accountia.expense.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;

import java.util.List;
import java.util.Map;

@Getter
@Builder
public class MonthlyReportResponse {

    @Schema(example = "2026")
    private int year;

    @Schema(example = "2")
    private int month;

    @Schema(example = "3450.0")
    private Double totalAmount;

    private Map<String, Double> totalsByStatus;

    private List<CategoryBreakdownResponse> categoryBreakdown;
}
