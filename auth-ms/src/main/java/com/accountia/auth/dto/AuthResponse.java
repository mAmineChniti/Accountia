package com.accountia.auth.dto;

import com.accountia.auth.model.User;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class AuthResponse {
    private String message;
    private User user;
    private TokenResponse tokens;
}
