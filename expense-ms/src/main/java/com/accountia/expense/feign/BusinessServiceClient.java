package com.accountia.expense.feign;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(
    name = "business-ms",
    path = "/api/businesses",
    configuration = com.accountia.expense.config.FeignConfig.class,
    fallbackFactory = BusinessServiceFallbackFactory.class
)
public interface BusinessServiceClient {

    @GetMapping("/{businessId}")
    BusinessResponse getBusiness(@PathVariable("businessId") Long businessId);
}
