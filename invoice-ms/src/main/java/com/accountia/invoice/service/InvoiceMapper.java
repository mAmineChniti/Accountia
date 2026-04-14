package com.accountia.invoice.service;

import com.accountia.invoice.domain.entity.Invoice;
import com.accountia.invoice.domain.entity.InvoiceComment;
import com.accountia.invoice.domain.entity.InvoiceLineItem;
import com.accountia.invoice.domain.entity.InvoiceReceipt;
import com.accountia.invoice.domain.entity.InvoiceRecipient;
import com.accountia.invoice.domain.entity.RecurringInvoice;
import com.accountia.invoice.dto.request.CreateLineItemRequest;
import com.accountia.invoice.dto.request.CreateRecipientRequest;
import com.accountia.invoice.dto.response.*;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

/**
 * Manual mapper: converts JPA entities ↔ DTOs.
 *
 * <p>We use a manual mapper instead of MapStruct for two reasons specific to this project:
 * <ol>
 *   <li>Every line is explainable in an oral exam — MapStruct generates code that
 *       is invisible during a code walkthrough.</li>
 *   <li>Some mappings involve business logic (JSON parsing, amount calculation)
 *       that MapStruct cannot handle declaratively.</li>
 * </ol>
 */
@Component
public class InvoiceMapper {

    private static final Logger log = LoggerFactory.getLogger(InvoiceMapper.class);
    private final ObjectMapper objectMapper;

    public InvoiceMapper(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    // ── Invoice ───────────────────────────────────────────────────────────────

    /** Converts an Invoice entity to the full response DTO (includes line items). */
    public InvoiceResponse toResponse(Invoice invoice) {
        return new InvoiceResponse(
                invoice.getId(),
                invoice.getIssuerBusinessId(),
                invoice.getInvoiceNumber(),
                invoice.getStatus(),
                invoice.getTotalAmount(),
                invoice.getCurrency(),
                invoice.getAmountPaid(),
                invoice.getIssuedDate(),
                invoice.getDueDate(),
                invoice.getDescription(),
                invoice.getPaymentTerms(),
                toRecipientResponse(invoice.getRecipient()),
                invoice.getLineItems().stream().map(this::toLineItemResponse).toList(),
                invoice.getCreatedBy(),
                invoice.getLastModifiedBy(),
                invoice.getLastStatusChangeAt(),
                invoice.getCreatedAt(),
                invoice.getUpdatedAt()
        );
    }

    /** Maps a recipient request DTO to the embedded entity. */
    public InvoiceRecipient toRecipientEntity(CreateRecipientRequest req) {
        return InvoiceRecipient.builder()
                .type(req.type())
                .email(req.email())
                .displayName(req.displayName())
                .platformId(req.platformId())
                // Resolution status starts as PENDING for platform types, null for EXTERNAL
                .resolutionStatus(req.type() != null && req.type().name().startsWith("PLATFORM")
                        ? com.accountia.invoice.domain.enums.RecipientResolutionStatus.PENDING
                        : null)
                .build();
    }

    /** Maps a line item request DTO to a new entity (amount computed from qty × price). */
    public InvoiceLineItem toLineItemEntity(CreateLineItemRequest req, int sortOrder) {
        BigDecimal amount = req.quantity().multiply(req.unitPrice());
        return InvoiceLineItem.builder()
                .productId(req.productId())
                .productName(req.productName())
                .quantity(req.quantity())
                .unitPrice(req.unitPrice())
                .amount(amount)
                .description(req.description())
                .sortOrder(sortOrder)
                .build();
    }

    private InvoiceRecipientResponse toRecipientResponse(InvoiceRecipient r) {
        if (r == null) return null;
        return new InvoiceRecipientResponse(
                r.getType(),
                r.getPlatformId(),
                r.getTenantDatabaseName(),
                r.getEmail(),
                r.getDisplayName(),
                r.getResolutionStatus(),
                r.getLastResolutionAttempt()
        );
    }

    private InvoiceLineItemResponse toLineItemResponse(InvoiceLineItem item) {
        return new InvoiceLineItemResponse(
                item.getId(),
                item.getProductId(),
                item.getProductName(),
                item.getQuantity(),
                item.getUnitPrice(),
                item.getAmount(),
                item.getDescription()
        );
    }

    // ── InvoiceReceipt ────────────────────────────────────────────────────────

    public InvoiceReceiptResponse toReceiptResponse(InvoiceReceipt receipt) {
        return new InvoiceReceiptResponse(
                receipt.getId(),
                receipt.getInvoice() != null ? receipt.getInvoice().getId() : null,
                receipt.getIssuerTenantDatabaseName(),
                receipt.getIssuerBusinessId(),
                receipt.getIssuerBusinessName(),
                receipt.getInvoiceNumber(),
                receipt.getTotalAmount(),
                receipt.getCurrency(),
                receipt.getIssuedDate(),
                receipt.getDueDate(),
                receipt.getInvoiceStatus(),
                receipt.isRecipientViewed(),
                receipt.getRecipientViewedAt(),
                receipt.getLastSyncedAt(),
                receipt.getCreatedAt()
        );
    }

    // ── Comment ───────────────────────────────────────────────────────────────

    public CommentResponse toCommentResponse(InvoiceComment comment) {
        // Parse the JSON mentions array back to a List<String>
        List<String> mentions = parseMentions(comment.getMentions());
        return new CommentResponse(
                comment.getId(),
                comment.getBusinessId(),
                comment.getEntityType(),
                comment.getEntityId(),
                comment.getAuthorId(),
                comment.getAuthorName(),
                comment.getBody(),
                comment.getParentId(),
                mentions,
                comment.isEdited(),
                comment.isDeleted(),
                comment.getCreatedAt(),
                comment.getUpdatedAt()
        );
    }

    /** Serializes a list of mention strings to JSON text for DB storage. */
    public String serializeMentions(List<String> mentions) {
        if (mentions == null || mentions.isEmpty()) return "[]";
        try {
            return objectMapper.writeValueAsString(mentions);
        } catch (Exception e) {
            log.warn("Failed to serialize mentions: {}", e.getMessage());
            return "[]";
        }
    }

    private List<String> parseMentions(String json) {
        if (json == null || json.isBlank()) return List.of();
        try {
            return objectMapper.readValue(json, new TypeReference<>() {});
        } catch (Exception e) {
            log.warn("Failed to parse mentions JSON: {}", e.getMessage());
            return List.of();
        }
    }

    // ── RecurringInvoice ──────────────────────────────────────────────────────

    @SuppressWarnings("unchecked")
    public RecurringInvoiceResponse toRecurringResponse(RecurringInvoice r) {
        List<RecurringLineItemResponse> lineItems = parseRecurringLineItems(r.getLineItemsJson());
        List<String> generatedIds = parseStringList(r.getGeneratedInvoiceIds());

        // Build the recipient map (frontend expects Record<string, unknown>)
        Map<String, Object> recipientMap = Map.of(
                "type", r.getRecipientType() != null ? r.getRecipientType().name() : "",
                "email", r.getRecipientEmail() != null ? r.getRecipientEmail() : "",
                "displayName", r.getRecipientDisplayName() != null ? r.getRecipientDisplayName() : "",
                "platformId", r.getRecipientPlatformId() != null ? r.getRecipientPlatformId() : ""
        );

        return new RecurringInvoiceResponse(
                r.getId(),
                r.getBusinessId(),
                r.getName(),
                r.getFrequency() != null ? r.getFrequency().name().toLowerCase() : null,
                r.getStatus(),
                r.getStartDate(),
                r.getEndCondition(),
                r.getMaxOccurrences(),
                r.getOccurrenceCount(),
                r.getEndDate(),
                r.getNextRunAt(),
                r.getLastRunAt(),
                lineItems,
                r.getTotalAmount(),
                r.getCurrency(),
                r.getDueDaysFromIssue(),
                recipientMap,
                r.getDescription(),
                r.getPaymentTerms(),
                r.isAutoIssue(),
                generatedIds,
                r.getCreatedBy(),
                r.getCreatedAt(),
                r.getUpdatedAt()
        );
    }

    private List<RecurringLineItemResponse> parseRecurringLineItems(String json) {
        if (json == null || json.isBlank()) return List.of();
        try {
            List<Map<String, Object>> raw = objectMapper.readValue(json, new TypeReference<>() {});
            return raw.stream().map(m -> new RecurringLineItemResponse(
                    (String) m.get("productId"),
                    (String) m.get("productName"),
                    toBigDecimal(m.get("quantity")),
                    toBigDecimal(m.get("unitPrice")),
                    toBigDecimal(m.get("amount")),
                    (String) m.get("description")
            )).toList();
        } catch (Exception e) {
            log.warn("Failed to parse recurring line items JSON: {}", e.getMessage());
            return List.of();
        }
    }

    private List<String> parseStringList(String json) {
        if (json == null || json.isBlank()) return List.of();
        try {
            return objectMapper.readValue(json, new TypeReference<>() {});
        } catch (Exception e) {
            return List.of();
        }
    }

    private BigDecimal toBigDecimal(Object value) {
        if (value == null) return BigDecimal.ZERO;
        if (value instanceof Number n) return new BigDecimal(n.toString());
        return BigDecimal.ZERO;
    }
}
