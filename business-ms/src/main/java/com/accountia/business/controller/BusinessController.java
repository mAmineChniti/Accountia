package com.accountia.business.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/business")
public class BusinessController {
    @GetMapping("/health")
    public ResponseEntity<?> health() {
        return ResponseEntity.ok("business-ms up");
    }
}
