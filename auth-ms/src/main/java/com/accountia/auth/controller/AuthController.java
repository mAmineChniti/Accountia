package com.accountia.auth.controller;

import com.accountia.auth.dto.UserDTO;
import com.accountia.auth.dto.RegisterDTO;
import com.accountia.auth.util.JwtUtil;
import com.accountia.auth.service.AuthService;
import com.accountia.auth.service.RateLimitingService;
import com.accountia.auth.repository.UserRepository;
import com.accountia.auth.service.RefreshTokenService;
import com.accountia.auth.model.RefreshToken;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.UUID;
import java.time.Instant;
import com.accountia.auth.repository.PasswordResetTokenRepository;
import com.accountia.auth.model.PasswordResetToken;
import org.springframework.security.crypto.password.PasswordEncoder;

@RestController
@RequestMapping("/api/auth")
public class AuthController {
    private final AuthService authService;
    private final JwtUtil jwtUtil;
    private final RateLimitingService rateLimitingService;
    private final RefreshTokenService refreshTokenService;
    private final UserRepository userRepository;
    private final PasswordResetTokenRepository passwordResetTokenRepository;
    private final PasswordEncoder passwordEncoder;

    public AuthController(AuthService authService, JwtUtil jwtUtil, RateLimitingService rateLimitingService, RefreshTokenService refreshTokenService, UserRepository userRepository, PasswordResetTokenRepository passwordResetTokenRepository, PasswordEncoder passwordEncoder) {
        this.authService = authService;
        this.jwtUtil = jwtUtil;
        this.rateLimitingService = rateLimitingService;
        this.refreshTokenService = refreshTokenService;
        this.userRepository = userRepository;
        this.passwordResetTokenRepository = passwordResetTokenRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody Map<String, String> body) {
        String email = body.get("email");
        String password = body.get("password");

        if (email == null || password == null || email.trim().isBlank() || password.isBlank()) {
            return ResponseEntity.status(400).body("Missing email or password");
        }
        email = email.trim();
        
        if (rateLimitingService.isRateLimited(email)) {
            return ResponseEntity.status(429).body("Too many login attempts. Please try again in " + rateLimitingService.getRemainingLockTime(email) + " seconds.");
        }
        
        var opt = authService.login(email, password);
        if (opt.isPresent()) {
            rateLimitingService.resetAttempts(email);
            com.accountia.auth.dto.UserDTO u = opt.get();
            com.accountia.auth.model.User fullUser = userRepository.findById(u.getId()).orElseThrow();
            java.util.Map<String, Object> claims = new java.util.HashMap<>();
            claims.put("userId", fullUser.getId());
            claims.put("tenantId", fullUser.getTenantId());
            claims.put("email", fullUser.getEmail());
            claims.put("role", fullUser.getRole());
            String accessToken = jwtUtil.generateTokenWithClaims(fullUser.getEmail(), claims);
            RefreshToken rt = refreshTokenService.createRefreshToken(fullUser);
            java.util.Map<String, Object> resp = new java.util.HashMap<>();
            resp.put("accessToken", accessToken);
            resp.put("refreshToken", rt.getToken());
            resp.put("user", u);
            return ResponseEntity.ok().body(resp);
        }
        rateLimitingService.recordFailedAttempt(email);
        return ResponseEntity.status(401).body("Invalid credentials or user not found");
    }

    @PostMapping("/refresh")
    public ResponseEntity<?> refresh(@RequestBody Map<String, String> body) {
        String token = body.get("refreshToken");
        if (token == null) return ResponseEntity.badRequest().body("refreshToken required");
        var opt = refreshTokenService.findByToken(token).filter(rt -> rt.getExpiryDate().isAfter(java.time.Instant.now()));
        if (opt.isPresent()) {
            RefreshToken rt = opt.get();
            String subject = rt.getUser().getId() + ":" + rt.getUser().getTenantId() + ":" + rt.getUser().getEmail();
            String accessToken = jwtUtil.generateToken(subject);
            return ResponseEntity.ok().body(java.util.Map.of("accessToken", accessToken));
        }
        return ResponseEntity.status(401).body("Invalid or expired refresh token");
    }

    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody RegisterDTO dto) {
        UserDTO created = authService.register(dto);
        return ResponseEntity.status(201).body(created);
    }

    @GetMapping("/health")
    public ResponseEntity<?> health() {
        return ResponseEntity.ok("auth-ms up");
    }

    @PostMapping("/password-reset/request")
    public ResponseEntity<?> requestReset(@RequestBody Map<String, String> body) {
        String email = body.get("email");
        if (email == null) return ResponseEntity.badRequest().body("email required");
        var opt = userRepository.findByEmail(email);
        if (opt.isPresent()) {
            var user = opt.get();
            PasswordResetToken t = new PasswordResetToken();
            t.setToken(UUID.randomUUID().toString());
            t.setExpiryDate(Instant.now().plusSeconds(3600));
            t.setUser(user);
            passwordResetTokenRepository.save(t);
            return ResponseEntity.ok().body(java.util.Map.of("resetToken", t.getToken()));
        }
        return ResponseEntity.status(404).body("user not found");
    }

    @PostMapping("/password-reset/confirm")
    public ResponseEntity<?> confirmReset(@RequestBody Map<String, String> body) {
        String token = body.get("token");
        String newPassword = body.get("password");
        if (token == null || newPassword == null) return ResponseEntity.badRequest().body("token and password required");
        var opt = passwordResetTokenRepository.findByToken(token).filter(t -> t.getExpiryDate().isAfter(Instant.now()));
        if (opt.isPresent()) {
            var t = opt.get();
            var user = t.getUser();
            user.setPasswordHash(passwordEncoder.encode(newPassword));
            userRepository.save(user);
            passwordResetTokenRepository.delete(t);
            return ResponseEntity.ok("password updated");
        }
        return ResponseEntity.status(400).body("invalid or expired token");
    }
}
