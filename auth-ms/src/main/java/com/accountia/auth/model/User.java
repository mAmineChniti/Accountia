package com.accountia.auth.model;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Table(name = "users")
@Data
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String username;
    private String email;
    private String passwordHash;
    private String tenantId;

    // comma separated roles, e.g. "PLATFORM_ADMIN,BUSINESS_OWNER"
    private String roles;
}
