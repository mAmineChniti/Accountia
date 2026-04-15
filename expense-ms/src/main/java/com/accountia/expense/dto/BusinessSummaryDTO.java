package com.accountia.expense.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BusinessSummaryDTO {
    private String id;
    private String name;
    private boolean active;
}
