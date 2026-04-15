package com.accountia.invoice.model;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDate;

@Entity
@Table(name = "invoices")
@Data
public class Invoice {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String tenantId;

    @Column(nullable = false)
    private String clientName;

    @Column(nullable = false)
    private Double amount;

    private String description;

    @Column(name = "issue_date")
    private LocalDate issueDate;

    @Column(name = "due_date")
    private LocalDate dueDate;

    @Column(nullable = false)
    private String status; // DRAFT, SENT, PAID, CANCELLED

    @Column(name = "owner_subject")
    private String ownerSubject;
}
