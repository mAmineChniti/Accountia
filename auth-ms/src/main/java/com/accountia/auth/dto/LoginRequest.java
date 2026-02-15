package com.accountia.auth.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class LoginRequest {
    
    @NotBlank(message = "Identifier is required")
    @Size(min = 5, message = "Identifier must be at least 5 characters")
    private String identifier;
    
    @NotBlank(message = "Password is required")
    private String password;
}
