package com.accountia.expense.feign;

import com.accountia.expense.dto.ClientDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(name = "client-ms", fallbackFactory = ClientFeignClientFallbackFactory.class)
public interface ClientFeignClient {

    @GetMapping("/api/client/clients/{id}")
    ResponseEntity<ClientDTO> getClientById(@PathVariable("id") Integer id);
}