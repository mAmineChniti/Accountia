package com.accountia.client.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/client")
public class ClientController {
    @GetMapping("/health")
    public ResponseEntity<?> health() {
        return ResponseEntity.ok("client-ms up");
    }
}
