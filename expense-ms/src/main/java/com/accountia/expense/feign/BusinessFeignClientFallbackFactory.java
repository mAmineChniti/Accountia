package com.accountia.expense.feign;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cloud.openfeign.FallbackFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

@Component
public class BusinessFeignClientFallbackFactory implements FallbackFactory<BusinessFeignClient> {

    private static final Logger log = LoggerFactory.getLogger(BusinessFeignClientFallbackFactory.class);

    @Override
    public BusinessFeignClient create(Throwable cause) {
        log.error("Feign fallback déclenché pour business-ms. Raison: {}", cause.getMessage());

        return id -> {
            log.warn("Fallback getBusinessById({}) - business-ms indisponible", id);
            return ResponseEntity.ok(null);
        };
    }
}