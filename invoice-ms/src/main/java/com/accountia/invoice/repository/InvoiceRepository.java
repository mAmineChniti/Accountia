package com.accountia.invoice.repository;

import com.accountia.invoice.domain.entity.Invoice;
import com.accountia.invoice.domain.enums.InvoiceStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

/**
 * Data access for Invoice entities.
 *
 * <p>Uses JPQL (not native SQL) so the queries are database-agnostic and
 * work with H2 in tests. All queries automatically respect the
 * {@code @SQLRestriction("is_deleted = false")} on the entity — deleted
 * invoices are invisible without an explicit native query.
 *
 * <p>JOIN FETCH is used in detail queries to eagerly load line items
 * in a single SQL statement, avoiding the N+1 problem.
 */
@Repository
public interface InvoiceRepository extends JpaRepository<Invoice, String> {

    // ── Listing (paginated) ────────────────────────────────────────────────────

    /**
     * Lists invoices for a business, optionally filtered by status.
     * When status is null, all statuses are returned.
     *
     * @param businessId issuer's business UUID
     * @param status     filter — null means "no filter"
     * @param pageable   page + size + sort
     */
    @Query("""
            SELECT i FROM Invoice i
            WHERE i.issuerBusinessId = :businessId
              AND (:status IS NULL OR i.status = :status)
            """)
    Page<Invoice> findByIssuerBusinessIdAndStatus(
            @Param("businessId") String businessId,
            @Param("status") InvoiceStatus status,
            Pageable pageable
    );

    /** Counts all non-deleted invoices for a business (for pagination total). */
    long countByIssuerBusinessId(String businessId);

    // ── Detail (with line items) ───────────────────────────────────────────────

    /**
     * Fetches a single invoice WITH its line items in one query (JOIN FETCH).
     * Without JOIN FETCH, accessing lineItems after the session closes would
     * throw LazyInitializationException.
     */
    @Query("""
            SELECT DISTINCT i FROM Invoice i
            LEFT JOIN FETCH i.lineItems li
            WHERE i.id = :id
              AND i.issuerBusinessId = :businessId
            """)
    Optional<Invoice> findByIdAndBusinessIdWithItems(
            @Param("id") String id,
            @Param("businessId") String businessId
    );

    // ── Overdue detection (used by scheduler) ─────────────────────────────────

    /**
     * Finds all ISSUED or VIEWED invoices past their due date.
     * The scheduler calls this nightly to mark them OVERDUE.
     *
     * @param statuses set of statuses to check ({ISSUED, VIEWED})
     * @param today    current date (injected by scheduler for testability)
     */
    @Query("""
            SELECT i FROM Invoice i
            WHERE i.status IN :statuses
              AND i.dueDate < :today
            """)
    List<Invoice> findOverdueInvoices(
            @Param("statuses") List<InvoiceStatus> statuses,
            @Param("today") LocalDate today
    );

    // ── AI feature: client payment history ────────────────────────────────────

    /**
     * Returns the total invoice count and paid invoice count for a specific recipient email.
     * Used by the AI payment prediction algorithm (Feature 1).
     *
     * <p>Returns {@code Object[]} with:
     * <ol>
     *   <li>index 0 — total invoices issued to this recipient ({@code Long})</li>
     *   <li>index 1 — number of invoices in PAID or ARCHIVED status ({@code Long})</li>
     *   <li>index 2 — always {@code null} (avgDaysToClose computed separately if needed)</li>
     * </ol>
     *
     * <p>Intentionally avoids DATEDIFF (MySQL-only) to stay JPQL-portable for H2 tests.
     * The AI service already handles a {@code null} avgDays with graceful fallback.
     */
    @Query("""
            SELECT
                COUNT(i),
                SUM(CASE WHEN i.status IN (
                    com.accountia.invoice.domain.enums.InvoiceStatus.PAID,
                    com.accountia.invoice.domain.enums.InvoiceStatus.ARCHIVED
                ) THEN 1L ELSE 0L END)
            FROM Invoice i
            WHERE i.recipient.email = :recipientEmail
              AND i.issuerBusinessId = :businessId
            """)
    Object[] getClientPaymentStats(
            @Param("recipientEmail") String recipientEmail,
            @Param("businessId") String businessId
    );

    // ── Anomaly detection: duplicate check ────────────────────────────────────

    /**
     * Checks if a similar invoice (same recipient + close amount) was issued
     * within the last 7 days — used to flag potential duplicates.
     */
    @Query("""
            SELECT COUNT(i) > 0 FROM Invoice i
            WHERE i.issuerBusinessId = :businessId
              AND i.recipient.email = :recipientEmail
              AND i.issuedDate >= :since
              AND ABS(i.totalAmount - :amount) / :amount < 0.1
              AND i.id <> :excludeId
            """)
    boolean existsSimilarRecentInvoice(
            @Param("businessId") String businessId,
            @Param("recipientEmail") String recipientEmail,
            @Param("amount") java.math.BigDecimal amount,
            @Param("since") LocalDate since,
            @Param("excludeId") String excludeId
    );

    // ── Import: unique number check ────────────────────────────────────────────

    boolean existsByInvoiceNumber(String invoiceNumber);

    // ── Number generation: find max number for this business/year ─────────────

    @Query("""
            SELECT MAX(i.invoiceNumber) FROM Invoice i
            WHERE i.issuerBusinessId = :businessId
              AND i.invoiceNumber LIKE :pattern
            """)
    Optional<String> findMaxInvoiceNumberForPattern(
            @Param("businessId") String businessId,
            @Param("pattern") String pattern   // e.g. "INV-2026-%"
    );
}
