package com.accountia.auth.dto;

import lombok.Data;

@Data
public class RegisterDTO {
    private String email;
    private String password;
    private String tenantId;
}
