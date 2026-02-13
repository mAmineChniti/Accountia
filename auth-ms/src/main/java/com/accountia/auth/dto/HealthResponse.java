package com.accountia.auth.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.Map;

@Data
@AllArgsConstructor
public class HealthResponse {
    private String status;
    private Map<String, Object> details;
}
