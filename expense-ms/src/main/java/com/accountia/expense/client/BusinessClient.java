package com.accountia.expense.client;

import com.accountia.expense.dto.BusinessSummaryDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(name = "business-ms", path = "/api/business")
public interface BusinessClient {

    @GetMapping("/internal/{id}")
    BusinessSummaryDTO getBusinessSummary(@PathVariable("id") String id);
}
