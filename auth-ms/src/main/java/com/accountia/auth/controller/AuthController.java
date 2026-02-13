package com.accountia.auth.controller;

import com.accountia.auth.dto.*;
import com.accountia.auth.model.User;
import com.accountia.auth.service.AuthService;
import com.accountia.auth.service.RateLimitingService;
import com.accountia.auth.service.RefreshTokenService;
import com.accountia.auth.repository.UserRepository;
import com.accountia.auth.repository.PasswordResetTokenRepository;
import com.accountia.auth.model.PasswordResetToken;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.Instant;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/auth")
public class AuthController {
    private final AuthService authService;
    private final RateLimitingService rateLimitingService;
    private final RefreshTokenService refreshTokenService;
    private final UserRepository userRepository;
    private final PasswordResetTokenRepository passwordResetTokenRepository;
    private final PasswordEncoder passwordEncoder;

    public AuthController(AuthService authService, RateLimitingService rateLimitingService, 
                         RefreshTokenService refreshTokenService, UserRepository userRepository,
                         PasswordResetTokenRepository passwordResetTokenRepository, 
                         PasswordEncoder passwordEncoder) {
        this.authService = authService;
        this.rateLimitingService = rateLimitingService;
        this.refreshTokenService = refreshTokenService;
        this.userRepository = userRepository;
        this.passwordResetTokenRepository = passwordResetTokenRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @PostMapping("/register")
    public ResponseEntity<?> register(@Valid @RequestBody RegisterRequest request) {
        try {
            User user = authService.register(request);
            return ResponseEntity.ok(new MessageResponse("Registration successful"));
        } catch (IllegalArgumentException e) {
            if (e.getMessage().contains("already exists")) {
                return ResponseEntity.status(409).body(new MessageResponse("Username or email is already registered"));
            }
            return ResponseEntity.status(500).body(new MessageResponse("Registration failed"));
        }
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@Valid @RequestBody LoginRequest request, HttpServletRequest httpRequest) {
        String clientIp = getClientIp(httpRequest);
        String key = clientIp + ":" + request.getIdentifier();
        
        if (rateLimitingService.isRateLimited(key)) {
            return ResponseEntity.status(429).body(new MessageResponse("Too many failed login attempts. Please try again later."));
        }
        
        try {
            var result = authService.login(request.getIdentifier(), request.getPassword());
            if (result.isPresent()) {
                User user = result.get();
                rateLimitingService.resetAttempts(key);
                
                TokenResponse tokens = refreshTokenService.createSession(user);
                
                return ResponseEntity.ok(new AuthResponse(
                    "Login successful",
                    user,
                    tokens
                ));
            }
        } catch (Exception e) {
            rateLimitingService.recordFailedAttempt(key);
        }
        
        rateLimitingService.recordFailedAttempt(key);
        return ResponseEntity.status(401).body(new MessageResponse("Username or email not found"));
    }

    @PostMapping("/password-reset/initiate")
    public ResponseEntity<?> passwordResetInitiate(@Valid @RequestBody PasswordResetRequest request) {
        try {
            authService.initiatePasswordReset(request.getEmail());
            return ResponseEntity.ok(new MessageResponse("If an account exists with this email, a reset link will be sent"));
        } catch (IllegalArgumentException e) {
            if (e.getMessage().contains("No user found")) {
                return ResponseEntity.status(404).body(new MessageResponse("No user found with this email"));
            }
            return ResponseEntity.status(500).body(new MessageResponse("Failed to send password reset email"));
        }
    }

    @PostMapping("/password-reset/confirm")
    public ResponseEntity<?> passwordResetConfirm(@Valid @RequestBody PasswordResetConfirmRequest request) {
        try {
            authService.confirmPasswordReset(request.getToken(), request.getNewPassword());
            return ResponseEntity.ok(new MessageResponse("Password reset successfully"));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(400).body(new MessageResponse("Invalid or expired reset token"));
        }
    }

    @GetMapping("/health")
    public ResponseEntity<?> health() {
        Map<String, Object> details = Map.of(
            "service", "auth-ms",
            "status", "healthy",
            "timestamp", Instant.now()
        );
        return ResponseEntity.ok(new HealthResponse("ok", details));
    }

    private String getClientIp(HttpServletRequest request) {
        String xForwardedFor = request.getHeader("X-Forwarded-For");
        if (xForwardedFor != null && !xForwardedFor.isEmpty()) {
            return xForwardedFor.split(",")[0].trim();
        }
        return request.getRemoteAddr();
    }
}
