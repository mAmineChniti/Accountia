package com.accountia.auth.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.time.Instant;

@Data
@AllArgsConstructor
public class TokenResponse {
    private String accessToken;
    private Instant accessExpiresAt;
    private String refreshToken;
    private Instant refreshExpiresAt;
}
