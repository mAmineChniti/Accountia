package com.accountia.expense.feign;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cloud.openfeign.FallbackFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

@Component
public class ClientFeignClientFallbackFactory implements FallbackFactory<ClientFeignClient> {

    private static final Logger log = LoggerFactory.getLogger(ClientFeignClientFallbackFactory.class);

    @Override
    public ClientFeignClient create(Throwable cause) {
        log.error("Feign fallback déclenché pour client-ms. Raison: {}", cause.getMessage());

        return id -> {
            log.warn("Fallback getClientById({}) - client-ms indisponible", id);
            return ResponseEntity.ok(null);
        };
    }
}