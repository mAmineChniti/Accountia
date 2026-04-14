package com.accountia.invoice.controller;

import com.accountia.invoice.domain.enums.InvoiceStatus;
import com.accountia.invoice.dto.response.InvoiceResponse;
import com.accountia.invoice.dto.response.ReceivedInvoiceListResponse;
import com.accountia.invoice.service.InvoiceReceiptService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * REST controller for the invoice recipient side (inbox of received invoices).
 *
 * <p>Platform recipients (businesses and individuals) can view invoices that were
 * issued to them. When a recipient opens an invoice detail, the receipt is marked
 * as "viewed" so the issuer can track delivery confirmation.
 *
 * <p>Two recipient types are supported:
 * <ul>
 *   <li><strong>Business inbox</strong> — used when {@code recipient.type = PLATFORM_BUSINESS},
 *       filtered by {@code recipientBusinessId}.</li>
 *   <li><strong>Individual inbox</strong> — used when {@code recipient.type = PLATFORM_INDIVIDUAL},
 *       filtered by {@code recipientUserId}.</li>
 * </ul>
 *
 * <p>External recipients (email-only) do NOT have an inbox — they receive invoices by email.
 */
@RestController
@RequestMapping("/invoices")
@Tag(name = "Invoices — Received", description = "Inbox of invoices received from other businesses")
@SecurityRequirement(name = "bearerAuth")
public class InvoiceReceiptController {

    private final InvoiceReceiptService receiptService;

    public InvoiceReceiptController(InvoiceReceiptService receiptService) {
        this.receiptService = receiptService;
    }

    // ── Business inbox ────────────────────────────────────────────────────────

    /**
     * Lists all invoices received by a business (paginated, optional status filter).
     *
     * <p>Called by the "Received Invoices" tab in the frontend for business accounts.
     */
    @Operation(
            summary = "List invoices received by a business",
            description = "Returns a paginated list of invoices where the recipient is this business. " +
                          "Optionally filter by invoice status."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Paginated received invoice list"),
            @ApiResponse(responseCode = "401", description = "Missing or invalid JWT token")
    })
    @GetMapping("/received/business")
    public ResponseEntity<ReceivedInvoiceListResponse> listReceivedByBusiness(
            @Parameter(description = "Recipient business UUID", required = true)
            @RequestParam String businessId,
            @Parameter(description = "Optional status filter")
            @RequestParam(required = false) InvoiceStatus status,
            @Parameter(description = "Page number, 1-based (default 1)")
            @RequestParam(defaultValue = "1") int page,
            @Parameter(description = "Page size (default 10)")
            @RequestParam(defaultValue = "10") int limit) {
        return ResponseEntity.ok(
                receiptService.listByBusiness(businessId, status, page, limit));
    }

    /**
     * Returns the full invoice details from a business receipt.
     * Marks the receipt as "viewed" on first access.
     *
     * @param receiptId  the InvoiceReceipt UUID (not the invoice UUID)
     * @param businessId the recipient business UUID (authorization guard)
     */
    @Operation(
            summary = "Get received invoice details (business)",
            description = "Returns full invoice details for a receipt in the business inbox. " +
                          "Marks the invoice as viewed on first access."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Invoice details",
                    content = @Content(schema = @Schema(implementation = InvoiceResponse.class))),
            @ApiResponse(responseCode = "404", description = "Receipt not found or not for this business")
    })
    @GetMapping("/received/business/{receiptId}")
    public ResponseEntity<InvoiceResponse> getReceivedByBusiness(
            @Parameter(description = "Receipt UUID") @PathVariable String receiptId,
            @Parameter(description = "Recipient business UUID", required = true)
            @RequestParam String businessId) {
        return ResponseEntity.ok(
                receiptService.getReceiptDetailsByBusiness(receiptId, businessId));
    }

    // ── Individual inbox ──────────────────────────────────────────────────────

    /**
     * Lists all invoices received by an individual user (paginated, optional status filter).
     *
     * <p>Called by the "Received Invoices" tab in the frontend for individual accounts.
     */
    @Operation(
            summary = "List invoices received by an individual user",
            description = "Returns a paginated list of invoices where the recipient is this user. " +
                          "Optionally filter by invoice status."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Paginated received invoice list"),
            @ApiResponse(responseCode = "401", description = "Missing or invalid JWT token")
    })
    @GetMapping("/received/individual")
    public ResponseEntity<ReceivedInvoiceListResponse> listReceivedByIndividual(
            @Parameter(description = "Recipient user UUID", required = true)
            @RequestParam String userId,
            @Parameter(description = "Optional status filter")
            @RequestParam(required = false) InvoiceStatus status,
            @Parameter(description = "Page number, 1-based (default 1)")
            @RequestParam(defaultValue = "1") int page,
            @Parameter(description = "Page size (default 10)")
            @RequestParam(defaultValue = "10") int limit) {
        return ResponseEntity.ok(
                receiptService.listByIndividual(userId, status, page, limit));
    }

    /**
     * Returns the full invoice details from an individual user's receipt.
     * Marks the receipt as "viewed" on first access.
     *
     * @param receiptId the InvoiceReceipt UUID
     * @param userId    the recipient user UUID (authorization guard)
     */
    @Operation(
            summary = "Get received invoice details (individual)",
            description = "Returns full invoice details for a receipt in the individual inbox. " +
                          "Marks the invoice as viewed on first access."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Invoice details",
                    content = @Content(schema = @Schema(implementation = InvoiceResponse.class))),
            @ApiResponse(responseCode = "404", description = "Receipt not found or not for this user")
    })
    @GetMapping("/received/individual/{receiptId}")
    public ResponseEntity<InvoiceResponse> getReceivedByIndividual(
            @Parameter(description = "Receipt UUID") @PathVariable String receiptId,
            @Parameter(description = "Recipient user UUID", required = true)
            @RequestParam String userId) {
        return ResponseEntity.ok(
                receiptService.getReceiptDetailsByIndividual(receiptId, userId));
    }
}
