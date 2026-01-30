package com.accountia.auth.controller;

import com.accountia.auth.dto.UserDTO;
import com.accountia.auth.service.AuthService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/auth")
public class AuthController {
    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody Map<String, String> body) {
        String email = body.get("email");
        String password = body.get("password");
        return authService.login(email, password)
                .map(u -> ResponseEntity.ok(u))
                .orElseGet(() -> ResponseEntity.status(401).body("Invalid credentials or user not found"));
    }

    @GetMapping("/health")
    public ResponseEntity<?> health() {
        return ResponseEntity.ok("auth-ms up");
    }
}
