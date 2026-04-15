package com.accountia.business.controller;

import com.accountia.business.dto.BusinessSummaryDTO;
import com.accountia.business.service.BusinessService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/business")
@RequiredArgsConstructor
public class BusinessController {

    private final BusinessService businessService;

    @GetMapping("/health")
    public ResponseEntity<?> health() {
        return ResponseEntity.ok("business-ms up");
    }

    /**
     * Internal endpoint for other microservices to fetch business summary.
     * Used for validation by services like expense-ms.
     */
    @GetMapping("/internal/{id}")
    public ResponseEntity<BusinessSummaryDTO> getBusinessInternal(@PathVariable String id) {
        return ResponseEntity.ok(businessService.getBusinessSummary(id));
    }
}
