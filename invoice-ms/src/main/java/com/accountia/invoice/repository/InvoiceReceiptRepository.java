package com.accountia.invoice.repository;

import com.accountia.invoice.domain.entity.InvoiceReceipt;
import com.accountia.invoice.domain.enums.InvoiceStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Data access for invoice receipts (received-invoice inbox).
 */
@Repository
public interface InvoiceReceiptRepository extends JpaRepository<InvoiceReceipt, String> {

    // ── Business recipient ─────────────────────────────────────────────────────

    @Query("""
            SELECT r FROM InvoiceReceipt r
            WHERE r.recipientBusinessId = :businessId
              AND (:status IS NULL OR r.invoiceStatus = :status)
            """)
    Page<InvoiceReceipt> findByRecipientBusinessId(
            @Param("businessId") String businessId,
            @Param("status") InvoiceStatus status,
            Pageable pageable
    );

    long countByRecipientBusinessId(String businessId);

    // ── Individual recipient ───────────────────────────────────────────────────

    @Query("""
            SELECT r FROM InvoiceReceipt r
            WHERE r.recipientUserId = :userId
              AND (:status IS NULL OR r.invoiceStatus = :status)
            """)
    Page<InvoiceReceipt> findByRecipientUserId(
            @Param("userId") String userId,
            @Param("status") InvoiceStatus status,
            Pageable pageable
    );

    long countByRecipientUserId(String userId);

    // ── Details ────────────────────────────────────────────────────────────────

    Optional<InvoiceReceipt> findByIdAndRecipientBusinessId(String id, String businessId);

    Optional<InvoiceReceipt> findByIdAndRecipientUserId(String id, String userId);

    // ── Sync: update all receipts when parent invoice status changes ───────────

    @Query("SELECT r FROM InvoiceReceipt r WHERE r.invoice.id = :invoiceId")
    List<InvoiceReceipt> findAllByInvoiceId(@Param("invoiceId") String invoiceId);
}
