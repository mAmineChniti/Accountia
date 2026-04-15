package com.accountia.expense.feign;

import com.accountia.expense.dto.BusinessDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(name = "business-ms", fallbackFactory = BusinessFeignClientFallbackFactory.class)
public interface BusinessFeignClient {

    @GetMapping("/api/business/businesses/{id}")
    ResponseEntity<BusinessDTO> getBusinessById(@PathVariable("id") Long id);
}