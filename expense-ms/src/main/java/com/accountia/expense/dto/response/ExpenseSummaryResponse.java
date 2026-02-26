package com.accountia.expense.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;

import java.util.Map;

@Getter
@Builder
public class ExpenseSummaryResponse {

    @Schema(example = "2450.75")
    private Double totalAmount;

    private Map<String, Double> totalsByStatus;

    private Map<Long, Double> totalsByCategory;
}
