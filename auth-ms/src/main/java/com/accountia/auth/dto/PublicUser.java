package com.accountia.auth.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.time.Instant;

@Data
@AllArgsConstructor
public class PublicUser {
    private String username;
    private String firstName;
    private String lastName;
    private String phoneNumber;
    private Instant createdAt;
    private boolean isActive;
}
