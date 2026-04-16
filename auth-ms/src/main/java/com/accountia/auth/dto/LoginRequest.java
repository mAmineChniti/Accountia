package com.accountia.auth.dto;

import com.fasterxml.jackson.annotation.JsonAlias;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class LoginRequest {
    
    @JsonAlias("email")
    @NotBlank(message = "Identifier is required")
    @Size(min = 5, message = "Identifier must be at least 5 characters")
    private String identifier;
    
    @NotBlank(message = "Password is required")
    private String password;
}
