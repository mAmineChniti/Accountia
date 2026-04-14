package com.accountia.invoice.controller;

import com.accountia.invoice.util.JwtUtil;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * Development-only authentication helper.
 *
 * <p>Generates a valid JWT token for testing purposes. This controller is used
 * ONLY in Sprint 1 while Keycloak is not yet integrated. In Sprint 2, this
 * will be removed and replaced by Keycloak's token endpoint via the API Gateway.
 *
 * <p>The generated token is signed with the same secret key as the JWT filter,
 * so it is fully accepted by all secured endpoints.
 */
@RestController
@RequestMapping("/auth")
@Tag(name = "Auth (Dev Only)", description = "Test token generator — Sprint 1 only, replaced by Keycloak in Sprint 2")
public class AuthController {

    private final JwtUtil jwtUtil;

    public AuthController(JwtUtil jwtUtil) {
        this.jwtUtil = jwtUtil;
    }

    /**
     * Generates a JWT token for testing.
     *
     * <p>Pass any email, userId, and username — the token will be valid for 24 hours.
     * Use the returned token as: {@code Authorization: Bearer <token>}
     *
     * <p>Example request body:
     * <pre>
     * {
     *   "email": "test@company.com",
     *   "userId": "user-001",
     *   "username": "testuser"
     * }
     * </pre>
     */
    @Operation(
            summary = "Generate a test JWT token (dev only)",
            description = "Returns a signed JWT token valid for 24h. " +
                          "Use it as 'Authorization: Bearer <token>' on all other endpoints. " +
                          "This endpoint is open (no auth required). " +
                          "Will be removed when Keycloak is integrated in Sprint 2."
    )
    @PostMapping("/token")
    public ResponseEntity<Map<String, String>> generateToken(
            @RequestBody Map<String, String> body) {

        String email    = body.getOrDefault("email",    "test@accountia.com");
        String userId   = body.getOrDefault("userId",   "user-001");
        String username = body.getOrDefault("username", "testuser");

        String token = jwtUtil.generateToken(email, userId, username);

        return ResponseEntity.ok(Map.of(
                "token",      token,
                "type",       "Bearer",
                "email",      email,
                "userId",     userId,
                "expiresIn",  "86400 seconds (24h)",
                "usage",      "Authorization: Bearer " + token
        ));
    }
}
