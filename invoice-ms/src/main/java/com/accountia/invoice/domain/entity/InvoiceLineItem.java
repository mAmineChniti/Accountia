package com.accountia.invoice.domain.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.UuidGenerator;

import java.math.BigDecimal;

/**
 * A single product/service line on an invoice.
 *
 * <p>{@code amount = quantity × unitPrice} is computed by the service and stored
 * so that aggregate queries (SUM of amounts) do not need to calculate on the fly.
 *
 * <p>The {@code invoice} relationship is LAZY — the parent Invoice entity loads
 * line items only when {@code invoice.getLineItems()} is called. The repository
 * uses JOIN FETCH in detail queries to avoid the N+1 problem.
 */
@Entity
@Table(name = "invoice_line_items")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class InvoiceLineItem {

    @Id
    @UuidGenerator
    @Column(name = "id", length = 36, updatable = false, nullable = false)
    private String id;

    /**
     * Back-reference to the parent invoice.
     * LAZY fetch: only loads the full Invoice when {@code getInvoice()} is called.
     * {@code @JoinColumn} specifies the foreign key column in THIS table.
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "invoice_id", nullable = false)
    private Invoice invoice;

    /** Reference to the product in the products microservice (denormalized here). */
    @Column(name = "product_id", nullable = false, length = 36)
    private String productId;

    /** Product name snapshot — stored so renames don't affect historical invoices. */
    @Column(name = "product_name", nullable = false, length = 200)
    private String productName;

    /** Number of units (can be fractional, e.g. 1.5 hours). */
    @Column(name = "quantity", nullable = false, precision = 10, scale = 2)
    private BigDecimal quantity;

    /** Price per unit at the time of invoicing. */
    @Column(name = "unit_price", nullable = false, precision = 15, scale = 3)
    private BigDecimal unitPrice;

    /**
     * Pre-computed amount: {@code quantity × unitPrice}.
     * Stored for query efficiency (avoids computed columns).
     */
    @Column(name = "amount", nullable = false, precision = 15, scale = 3)
    private BigDecimal amount;

    /** Optional description / notes for this line item. */
    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    /** Controls display order in the invoice PDF and UI. */
    @Column(name = "sort_order", nullable = false)
    @Builder.Default
    private int sortOrder = 0;

    /** Recomputes amount from quantity × unitPrice. Call whenever either changes. */
    public void computeAmount() {
        this.amount = quantity.multiply(unitPrice);
    }
}
