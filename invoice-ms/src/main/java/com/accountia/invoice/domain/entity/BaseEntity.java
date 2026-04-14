package com.accountia.invoice.domain.entity;

import jakarta.persistence.Column;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.MappedSuperclass;
import lombok.Getter;
import lombok.Setter;
import org.springframework.data.annotation.CreatedBy;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedBy;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.Instant;

/**
 * Shared auditing fields inherited by all entities.
 *
 * <p>Spring JPA Auditing (enabled via {@code @EnableJpaAuditing} in InvoiceApplication)
 * automatically fills {@code createdAt}, {@code updatedAt}, {@code createdBy},
 * and {@code lastModifiedBy} using the {@link AuditingEntityListener}.
 *
 * <p>{@code @MappedSuperclass} means this class has no table of its own —
 * its columns are added directly to each subclass's table.
 */
@MappedSuperclass
@EntityListeners(AuditingEntityListener.class)
@Getter
@Setter
public abstract class BaseEntity {

    /** When this record was first saved. Set automatically by JPA Auditing. */
    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    /** When this record was last updated. Auto-updated by JPA Auditing. */
    @LastModifiedDate
    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    /** Email of the user who created this record (from JWT principal). */
    @CreatedBy
    @Column(name = "created_by", length = 150)
    private String createdBy;

    /** Email of the user who last modified this record (from JWT principal). */
    @LastModifiedBy
    @Column(name = "last_modified_by", length = 150)
    private String lastModifiedBy;
}
