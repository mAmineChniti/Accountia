package com.accountia.auth.controller;

import com.accountia.auth.dto.*;
import com.accountia.auth.model.User;
import com.accountia.auth.service.AuthService;
import com.accountia.auth.service.RefreshTokenService;
import com.accountia.auth.repository.UserRepository;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;

@RestController
@RequestMapping("/api/v1")
public class UserController {
    private final AuthService authService;
    private final RefreshTokenService refreshTokenService;
    private final UserRepository userRepository;

    public UserController(AuthService authService, RefreshTokenService refreshTokenService, UserRepository userRepository) {
        this.authService = authService;
        this.refreshTokenService = refreshTokenService;
        this.userRepository = userRepository;
    }

    @GetMapping("/fetchuser")
    public ResponseEntity<?> fetchUser(@AuthenticationPrincipal UserDetails userDetails) {
        try {
            User user = userRepository.findByEmail(userDetails.getUsername())
                .orElseThrow(() -> new IllegalArgumentException("User not found"));
            
            PublicUser publicUser = new PublicUser(
                user.getUsername(),
                user.getFirstName(),
                user.getLastName(),
                user.getPhoneNumber(),
                user.getCreatedAt(),
                user.getIsActive()
            );
            
            return ResponseEntity.ok(new UserResponse("User profile retrieved successfully", publicUser));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(404).body(new MessageResponse("Your user profile could not be retrieved"));
        }
    }

    @PostMapping("/fetchuserbyid")
    public ResponseEntity<?> fetchUserById(@Valid @RequestBody UserIdRequest request) {
        try {
            User user = userRepository.findById(request.getUserId())
                .orElseThrow(() -> new IllegalArgumentException("User not found"));
            
            PublicUser publicUser = new PublicUser(
                user.getUsername(),
                user.getFirstName(),
                user.getLastName(),
                user.getPhoneNumber(),
                user.getCreatedAt(),
                user.getIsActive()
            );
            
            return ResponseEntity.ok(new UserResponse("User fetched successfully", publicUser));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(404).body(new MessageResponse("The specified user could not be found"));
        }
    }

    @PutMapping("/update")
    @PatchMapping("/update")
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

    @GetMapping("/refresh")
    public ResponseEntity<?> refreshToken(@AuthenticationPrincipal UserDetails userDetails) {
        try {
            User user = userRepository.findByEmail(userDetails.getUsername())
                .orElseThrow(() -> new IllegalArgumentException("User not found"));
            
            TokenResponse tokens = refreshTokenService.createSession(user);
            return ResponseEntity.ok(new com.accountia.auth.dto.AuthResponse(
                "Tokens refreshed successfully",
                null,
                tokens
            ));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(500).body(new MessageResponse("Unable to refresh authentication tokens"));
        }
    }

    // DTO for user ID request
    public static class UserIdRequest {
        private Long userId;

        public Long getUserId() {
            return userId;
        }

        public void setUserId(Long userId) {
            this.userId = userId;
        }
    }
}
