package com.accountia.auth.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class UpdateRequest {
    
    @Size(min = 5, max = 20, message = "Username must be between 5 and 20 characters")
    private String username;
    
    @Email(message = "Invalid email format")
    private String email;
    
    private String password;
    
    @Size(min = 2, max = 50, message = "First name must be between 2 and 50 characters")
    private String firstName;
    
    @Size(min = 2, max = 50, message = "Last name must be between 2 and 50 characters")
    private String lastName;
    
    private String phoneNumber;
}
