package com.accountia.auth.controller;

import com.accountia.auth.dto.*;
import com.accountia.auth.model.User;
import com.accountia.auth.service.AuthService;
import com.accountia.auth.service.RateLimitingService;
import com.accountia.auth.service.RefreshTokenService;
import com.accountia.auth.repository.UserRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.util.Map;

@RestController
@RequestMapping("/api/auth")
@Tag(name = "Authentication", description = "Auth endpoints: register, login, update, delete")
public class AuthController {
    private static final Logger logger = LoggerFactory.getLogger(AuthController.class);
    private final AuthService authService;
    private final RateLimitingService rateLimitingService;
    private final RefreshTokenService refreshTokenService;
    private final UserRepository userRepository;

    public AuthController(AuthService authService, RateLimitingService rateLimitingService,
                         RefreshTokenService refreshTokenService, UserRepository userRepository) {
        this.authService = authService;
        this.rateLimitingService = rateLimitingService;
        this.refreshTokenService = refreshTokenService;
        this.userRepository = userRepository;
    }

    @Operation(summary = "Register a new user", description = "Creates a new user account")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Registration successful",
            content = @Content(schema = @Schema(implementation = MessageResponse.class))),
        @ApiResponse(responseCode = "409", description = "Username or email already registered"),
        @ApiResponse(responseCode = "400", description = "Validation error")
    })
    @PostMapping("/register")
    public ResponseEntity<?> register(@Valid @RequestBody RegisterRequest request) {
        try {
            authService.register(request);
            return ResponseEntity.ok(new MessageResponse("Registration successful"));
        } catch (IllegalArgumentException e) {
            if (e.getMessage().contains("already exists")) {
                return ResponseEntity.status(409).body(new MessageResponse("Username or email is already registered"));
            }
            return ResponseEntity.status(500).body(new MessageResponse("Registration failed"));
        }
    }

    @Operation(summary = "Login", description = "Authenticate with email/username and password, returns JWT tokens")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Login successful",
            content = @Content(schema = @Schema(implementation = AuthResponse.class))),
        @ApiResponse(responseCode = "401", description = "Invalid credentials"),
        @ApiResponse(responseCode = "429", description = "Too many failed attempts")
    })
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
            logger.debug("Login failed: user not found or password mismatch for identifier: {}", request.getIdentifier());
        } catch (Exception e) {
            logger.error("Login error for identifier {}: {}", request.getIdentifier(), e.getMessage(), e);
        }

        rateLimitingService.recordFailedAttempt(key);
        return ResponseEntity.status(401).body(new MessageResponse("Username or email not found"));
    }

    @Operation(summary = "Update profile", description = "Update the authenticated user's profile",
        security = @SecurityRequirement(name = "bearerAuth"))
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Profile updated",
            content = @Content(schema = @Schema(implementation = UserResponse.class))),
        @ApiResponse(responseCode = "404", description = "User not found"),
        @ApiResponse(responseCode = "409", description = "Username or email already taken")
    })
    @PutMapping("/update")
    public ResponseEntity<?> update(@Valid @RequestBody UpdateRequest request,
                                   @AuthenticationPrincipal UserDetails userDetails) {
        try {
            User user = userRepository.findByEmail(userDetails.getUsername())
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

            User updatedUser = authService.updateUser(user.getId(), request);

            PublicUser publicUser = new PublicUser(
                updatedUser.getUsername(),
                updatedUser.getFirstName(),
                updatedUser.getLastName(),
                updatedUser.getPhoneNumber(),
                updatedUser.getCreatedAt(),
                updatedUser.getIsActive()
            );

            return ResponseEntity.ok(new UserResponse("Profile updated successfully", publicUser));
        } catch (IllegalArgumentException e) {
            if (e.getMessage().contains("not found")) {
                return ResponseEntity.status(404).body(new MessageResponse("Your user profile could not be found"));
            }
            if (e.getMessage().contains("already exists")) {
                return ResponseEntity.status(409).body(new MessageResponse("Username or email is already taken"));
            }
            return ResponseEntity.status(500).body(new MessageResponse("Profile update failed"));
        }
    }

    @Operation(summary = "Delete account", description = "Delete the authenticated user's account",
        security = @SecurityRequirement(name = "bearerAuth"))
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Account deleted",
            content = @Content(schema = @Schema(implementation = MessageResponse.class))),
        @ApiResponse(responseCode = "500", description = "Deletion failed")
    })
    @DeleteMapping("/delete")
    public ResponseEntity<?> delete(@AuthenticationPrincipal UserDetails userDetails) {
        try {
            User user = userRepository.findByEmail(userDetails.getUsername())
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

            authService.deleteUser(user.getId());
            return ResponseEntity.ok(new MessageResponse("Account deleted successfully"));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(500).body(new MessageResponse("Account deletion failed"));
        }
    }

    @Operation(summary = "Health check", description = "Check if the auth service is healthy")
    @ApiResponse(responseCode = "200", description = "Service is healthy",
        content = @Content(schema = @Schema(implementation = HealthResponse.class)))
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
