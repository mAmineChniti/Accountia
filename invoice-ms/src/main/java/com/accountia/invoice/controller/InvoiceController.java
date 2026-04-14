package com.accountia.invoice.controller;

import com.accountia.invoice.domain.enums.InvoiceStatus;
import com.accountia.invoice.dto.request.CreateInvoiceRequest;
import com.accountia.invoice.dto.request.TransitionInvoiceRequest;
import com.accountia.invoice.dto.request.UpdateInvoiceRequest;
import com.accountia.invoice.dto.response.BulkImportResponse;
import com.accountia.invoice.dto.response.InvoiceListResponse;
import com.accountia.invoice.dto.response.InvoiceResponse;
import com.accountia.invoice.service.InvoiceImportService;
import com.accountia.invoice.service.InvoiceService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

/**
 * REST controller for the invoice issuer side — creating, listing, updating,
 * and transitioning invoices that this business has issued.
 *
 * <p>All endpoints are under {@code /invoices} because the Next.js frontend
 * uses {@code BASE_URL = http://127.0.0.1:4789/api} and calls paths like
 * {@code /invoices/issued}, {@code /invoices/issued/{id}}, etc.
 *
 * <p>The {@code businessId} query parameter identifies the currently signed-in
 * business. In Sprint 2, this will be extracted from the Keycloak JWT instead.
 */
@RestController
@RequestMapping("/invoices")
@Tag(name = "Invoices — Issuer", description = "Create and manage invoices you have issued")
@SecurityRequirement(name = "bearerAuth")
public class InvoiceController {

    private final InvoiceService invoiceService;
    private final InvoiceImportService importService;

    public InvoiceController(InvoiceService invoiceService, InvoiceImportService importService) {
        this.invoiceService = invoiceService;
        this.importService = importService;
    }

    // ── Health ────────────────────────────────────────────────────────────────

    @Operation(summary = "Health check", description = "Returns OK if the service is running")
    @GetMapping("/health")
    public ResponseEntity<String> health() {
        return ResponseEntity.ok("invoice-ms up");
    }

    // ── CREATE ────────────────────────────────────────────────────────────────

    /**
     * Creates a new invoice in DRAFT status.
     *
     * <p>The invoice number is auto-generated (INV-YYYY-NNNNN).
     * Line item amounts are computed as quantity × unitPrice.
     * The invoice total is the sum of all line item amounts.
     */
    @Operation(
            summary = "Create a new invoice",
            description = "Creates a draft invoice with auto-generated invoice number. " +
                          "Line item amounts and total are computed automatically."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Invoice created",
                    content = @Content(schema = @Schema(implementation = InvoiceResponse.class))),
            @ApiResponse(responseCode = "400", description = "Validation error or invalid business rule"),
            @ApiResponse(responseCode = "401", description = "Missing or invalid JWT token")
    })
    @PostMapping
    public ResponseEntity<InvoiceResponse> createInvoice(
            @Valid @RequestBody CreateInvoiceRequest request) {
        InvoiceResponse response = invoiceService.createInvoice(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    // ── LIST ──────────────────────────────────────────────────────────────────

    /**
     * Lists issued invoices for a business, with optional status filter and pagination.
     *
     * <p>The frontend passes {@code page=1} for the first page. This is converted
     * internally to 0-based Spring Data pagination.
     */
    @Operation(
            summary = "List issued invoices",
            description = "Returns a paginated list of invoices issued by the given business. " +
                          "Optionally filter by status. Sorted by creation date descending."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Paginated invoice list"),
            @ApiResponse(responseCode = "401", description = "Missing or invalid JWT token")
    })
    @GetMapping("/issued")
    public ResponseEntity<InvoiceListResponse> listIssuedInvoices(
            @Parameter(description = "Business UUID", required = true)
            @RequestParam String businessId,
            @Parameter(description = "Filter by status (optional)")
            @RequestParam(required = false) InvoiceStatus status,
            @Parameter(description = "Page number, 1-based (default 1)")
            @RequestParam(defaultValue = "1") int page,
            @Parameter(description = "Page size (default 10)")
            @RequestParam(defaultValue = "10") int limit) {
        return ResponseEntity.ok(
                invoiceService.listIssuedInvoices(businessId, status, page, limit));
    }

    // ── GET SINGLE ────────────────────────────────────────────────────────────

    /**
     * Returns a single issued invoice with all its line items.
     *
     * @param id         invoice UUID
     * @param businessId must match the invoice's issuer (authorization guard)
     */
    @Operation(
            summary = "Get issued invoice by ID",
            description = "Returns full invoice details including line items. " +
                          "The businessId must match the issuer of this invoice."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Invoice found",
                    content = @Content(schema = @Schema(implementation = InvoiceResponse.class))),
            @ApiResponse(responseCode = "404", description = "Invoice not found or not owned by this business"),
            @ApiResponse(responseCode = "401", description = "Missing or invalid JWT token")
    })
    @GetMapping("/issued/{id}")
    public ResponseEntity<InvoiceResponse> getIssuedInvoice(
            @Parameter(description = "Invoice UUID") @PathVariable String id,
            @Parameter(description = "Business UUID", required = true) @RequestParam String businessId) {
        return ResponseEntity.ok(invoiceService.getIssuedInvoice(id, businessId));
    }

    // ── UPDATE (PATCH) ────────────────────────────────────────────────────────

    /**
     * Partially updates a DRAFT invoice.
     *
     * <p>Only description, paymentTerms, and dueDate can be changed.
     * Issued or paid invoices cannot be edited.
     */
    @Operation(
            summary = "Update a DRAFT invoice",
            description = "Allows editing description, paymentTerms, and dueDate on a DRAFT invoice. " +
                          "Null fields are ignored (PATCH semantics). Non-DRAFT invoices are rejected."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Invoice updated"),
            @ApiResponse(responseCode = "400", description = "Invoice is not in DRAFT status or invalid date"),
            @ApiResponse(responseCode = "404", description = "Invoice not found")
    })
    @PatchMapping("/issued/{id}")
    public ResponseEntity<InvoiceResponse> updateInvoice(
            @Parameter(description = "Invoice UUID") @PathVariable String id,
            @Valid @RequestBody UpdateInvoiceRequest request) {
        return ResponseEntity.ok(invoiceService.updateInvoice(id, request));
    }

    // ── STATUS TRANSITION ─────────────────────────────────────────────────────

    /**
     * Transitions an invoice to a new status.
     *
     * <p>The allowed transitions are defined by the state machine in {@code InvoiceStatus}.
     * Examples: DRAFT→ISSUED, ISSUED→PAID, ISSUED→CANCELLED.
     * Invalid transitions return 400 Bad Request.
     */
    @Operation(
            summary = "Transition invoice status",
            description = "Changes the invoice status according to the allowed state machine. " +
                          "For PARTIAL status, provide amountPaid. For PAID, amountPaid defaults to totalAmount. " +
                          "Every transition is logged in the status history audit trail."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Status transitioned successfully"),
            @ApiResponse(responseCode = "400", description = "Transition not allowed or invalid amount"),
            @ApiResponse(responseCode = "404", description = "Invoice not found")
    })
    @PostMapping("/issued/{id}/transition")
    public ResponseEntity<InvoiceResponse> transitionInvoice(
            @Parameter(description = "Invoice UUID") @PathVariable String id,
            @Valid @RequestBody TransitionInvoiceRequest request) {
        return ResponseEntity.ok(invoiceService.transitionInvoice(id, request));
    }

    // ── DELETE (SOFT) ─────────────────────────────────────────────────────────

    /**
     * Soft-deletes an invoice.
     *
     * <p>Sets {@code isDeleted=true} and {@code deletedAt} timestamp.
     * The invoice becomes invisible to all queries thanks to the
     * {@code @SQLRestriction} on the entity. Data is never physically removed.
     */
    @Operation(
            summary = "Delete an invoice (soft delete)",
            description = "Marks the invoice as deleted. The record is retained in the database " +
                          "but excluded from all queries. This action cannot be undone through the API."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Invoice deleted"),
            @ApiResponse(responseCode = "404", description = "Invoice not found")
    })
    @DeleteMapping("/issued/{id}")
    public ResponseEntity<Void> deleteInvoice(
            @Parameter(description = "Invoice UUID") @PathVariable String id,
            @Parameter(description = "Business UUID", required = true) @RequestParam String businessId) {
        invoiceService.deleteInvoice(id, businessId);
        return ResponseEntity.noContent().build();
    }

    // ── BULK IMPORT ───────────────────────────────────────────────────────────

    /**
     * Imports multiple invoices from a CSV or Excel file.
     *
     * <p>Partial success is supported: rows that fail validation are recorded
     * in the result but do not prevent other rows from being imported.
     *
     * <p>Required columns: recipientType, issuedDate (YYYY-MM-DD), dueDate (YYYY-MM-DD)
     * <br>Optional: invoiceNumber, recipientEmail, recipientDisplayName,
     * productNames, quantities, unitPrices, description, paymentTerms, currency
     */
    @Operation(
            summary = "Bulk import invoices from CSV or Excel",
            description = "Accepts a .csv or .xlsx file. Each row becomes one invoice. " +
                          "Partial success is allowed — failed rows are reported individually. " +
                          "Required columns: recipientType, issuedDate, dueDate."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Import processed (check result for per-row status)"),
            @ApiResponse(responseCode = "400", description = "Unsupported file format")
    })
    @PostMapping(value = "/import", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<BulkImportResponse> importInvoices(
            @Parameter(description = "CSV or XLSX file to import")
            @RequestParam("file") MultipartFile file,
            @Parameter(description = "Business UUID", required = true)
            @RequestParam String businessId) {
        return ResponseEntity.ok(importService.importInvoices(file, businessId));
    }
}
