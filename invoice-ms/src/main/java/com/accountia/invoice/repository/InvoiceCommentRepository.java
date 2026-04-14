package com.accountia.invoice.repository;

import com.accountia.invoice.domain.entity.InvoiceComment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/** Data access for invoice (and other entity) comments. */
@Repository
public interface InvoiceCommentRepository extends JpaRepository<InvoiceComment, String> {

    /**
     * Fetches all non-deleted comments for a given entity, ordered by creation time.
     * Soft-deleted comments are excluded.
     */
    @Query("""
            SELECT c FROM InvoiceComment c
            WHERE c.businessId = :businessId
              AND c.entityType = :entityType
              AND c.entityId = :entityId
              AND c.isDeleted = false
            ORDER BY c.createdAt ASC
            """)
    List<InvoiceComment> findActiveComments(
            @Param("businessId") String businessId,
            @Param("entityType") String entityType,
            @Param("entityId") String entityId
    );

    /** Finds a comment by ID, but only if it belongs to the given business (authorization). */
    Optional<InvoiceComment> findByIdAndBusinessId(String id, String businessId);
}
