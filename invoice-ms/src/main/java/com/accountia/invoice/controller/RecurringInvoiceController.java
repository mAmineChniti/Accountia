package com.accountia.invoice.controller;

import com.accountia.invoice.dto.response.RecurringInvoiceListResponse;
import com.accountia.invoice.dto.response.RecurringInvoiceResponse;
import com.accountia.invoice.service.RecurringInvoiceService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * REST controller for recurring invoice schedules.
 *
 * <p>A recurring invoice schedule defines a template and a frequency
 * (daily/weekly/monthly/quarterly/yearly). A nightly scheduler reads all active
 * schedules whose {@code nextRunAt} is in the past and auto-generates a new DRAFT invoice.
 *
 * <p>The request body uses a generic {@code Map<String, Object>} because the frontend
 * sends a free-form JSON object for schedule configuration. The service maps the known
 * keys to the entity fields.
 *
 * <p>Supported fields in the request body:
 * <ul>
 *   <li>{@code businessId} (String, required) — the owning business UUID</li>
 *   <li>{@code name} (String) — descriptive name for the schedule</li>
 *   <li>{@code frequency} (String) — DAILY/WEEKLY/MONTHLY/QUARTERLY/YEARLY</li>
 *   <li>{@code startDate} (String, ISO date) — when the schedule starts</li>
 *   <li>{@code endDate} (String, ISO date, optional) — when the schedule ends</li>
 *   <li>{@code endCondition} (String) — NEVER/DATE/OCCURRENCES</li>
 *   <li>{@code maxOccurrences} (Integer, optional) — max invoices to generate</li>
 *   <li>{@code currency} (String) — e.g. "TND"</li>
 *   <li>{@code dueDaysFromIssue} (Integer) — how many days after issue the invoice is due</li>
 *   <li>{@code autoIssue} (Boolean) — auto-transition generated invoices to ISSUED</li>
 *   <li>{@code description} (String, optional)</li>
 *   <li>{@code paymentTerms} (String, optional)</li>
 *   <li>{@code recipient} (Object) — { type, email, displayName, platformId }</li>
 *   <li>{@code lineItems} (Array) — [ { productName, quantity, unitPrice }, ... ]</li>
 * </ul>
 */
@RestController
@RequestMapping("/invoices/recurring")
@Tag(name = "Recurring Invoices", description = "Manage recurring invoice schedules")
@SecurityRequirement(name = "bearerAuth")
public class RecurringInvoiceController {

    private final RecurringInvoiceService recurringService;

    public RecurringInvoiceController(RecurringInvoiceService recurringService) {
        this.recurringService = recurringService;
    }

    // ── LIST ──────────────────────────────────────────────────────────────────

    /**
     * Lists all recurring invoice schedules for a business (paginated).
     */
    @Operation(
            summary = "List recurring invoice schedules",
            description = "Returns all recurring schedules for the given business, sorted by creation date descending."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Paginated list of recurring schedules"),
            @ApiResponse(responseCode = "401", description = "Missing or invalid JWT token")
    })
    @GetMapping
    public ResponseEntity<RecurringInvoiceListResponse> list(
            @Parameter(description = "Business UUID", required = true)
            @RequestParam String businessId,
            @Parameter(description = "Page number, 1-based (default 1)")
            @RequestParam(defaultValue = "1") int page,
            @Parameter(description = "Page size (default 10)")
            @RequestParam(defaultValue = "10") int limit) {
        return ResponseEntity.ok(recurringService.list(businessId, page, limit));
    }

    // ── GET SINGLE ────────────────────────────────────────────────────────────

    /**
     * Returns a single recurring schedule by ID.
     */
    @Operation(
            summary = "Get recurring schedule by ID",
            description = "Returns the full details of a recurring invoice schedule."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Recurring schedule found",
                    content = @Content(schema = @Schema(implementation = RecurringInvoiceResponse.class))),
            @ApiResponse(responseCode = "404", description = "Schedule not found")
    })
    @GetMapping("/{id}")
    public ResponseEntity<RecurringInvoiceResponse> getById(
            @Parameter(description = "Recurring schedule UUID") @PathVariable String id,
            @Parameter(description = "Business UUID", required = true) @RequestParam String businessId) {
        return ResponseEntity.ok(recurringService.getById(id, businessId));
    }

    // ── CREATE ────────────────────────────────────────────────────────────────

    /**
     * Creates a new recurring invoice schedule.
     *
     * <p>The request body is a free-form JSON object. See class-level Javadoc for
     * the list of supported fields.
     */
    @Operation(
            summary = "Create a recurring invoice schedule",
            description = "Creates a new schedule. The body is a free-form JSON object with the schedule " +
                          "configuration. At minimum, provide businessId, frequency, startDate, and lineItems."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Recurring schedule created",
                    content = @Content(schema = @Schema(implementation = RecurringInvoiceResponse.class))),
            @ApiResponse(responseCode = "400", description = "Invalid schedule data")
    })
    @PostMapping
    public ResponseEntity<RecurringInvoiceResponse> create(
            @RequestBody Map<String, Object> data) {
        RecurringInvoiceResponse response = recurringService.create(data);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    // ── UPDATE ────────────────────────────────────────────────────────────────

    /**
     * Updates an existing recurring schedule.
     *
     * <p>Only the fields present in the request body are updated.
     * Fields not included in the request are left unchanged.
     */
    @Operation(
            summary = "Update a recurring schedule",
            description = "Updates the provided fields of an existing recurring schedule. " +
                          "Only fields present in the request body are changed."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Schedule updated"),
            @ApiResponse(responseCode = "404", description = "Schedule not found")
    })
    @PutMapping("/{id}")
    public ResponseEntity<RecurringInvoiceResponse> update(
            @Parameter(description = "Recurring schedule UUID") @PathVariable String id,
            @RequestBody Map<String, Object> data) {
        // Ensure the path variable ID and the body are consistent
        data.put("id", id);
        return ResponseEntity.ok(recurringService.update(id, data));
    }

    // ── DELETE ────────────────────────────────────────────────────────────────

    /**
     * Deletes a recurring schedule permanently.
     *
     * <p>Note: this is a hard delete (no soft delete for recurring schedules),
     * because schedules are operational config, not financial records.
     */
    @Operation(
            summary = "Delete a recurring schedule",
            description = "Permanently removes a recurring invoice schedule. " +
                          "Already-generated invoices are not affected."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Schedule deleted"),
            @ApiResponse(responseCode = "404", description = "Schedule not found")
    })
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(
            @Parameter(description = "Recurring schedule UUID") @PathVariable String id,
            @Parameter(description = "Business UUID", required = true) @RequestParam String businessId) {
        recurringService.delete(id, businessId);
        return ResponseEntity.noContent().build();
    }
}
