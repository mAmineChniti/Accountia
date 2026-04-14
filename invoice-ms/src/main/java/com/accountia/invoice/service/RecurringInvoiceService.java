package com.accountia.invoice.service;

import com.accountia.invoice.domain.entity.RecurringInvoice;
import com.accountia.invoice.domain.enums.RecipientType;
import com.accountia.invoice.domain.enums.RecurringFrequency;
import com.accountia.invoice.dto.response.RecurringInvoiceListResponse;
import com.accountia.invoice.dto.response.RecurringInvoiceResponse;
import com.accountia.invoice.exception.ResourceNotFoundException;
import com.accountia.invoice.repository.RecurringInvoiceRepository;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

/**
 * Manages recurring invoice schedules.
 *
 * <p>The frontend sends schedule data as a generic {@code Record<string, unknown>}
 * (untyped map). This service reads the known fields by key and maps them to
 * the entity. Unknown fields are ignored.
 *
 * <p>A nightly scheduler (in OverdueAndRecurringScheduler) checks all active
 * schedules whose nextRunAt is in the past and auto-generates invoices.
 */
@Service
public class RecurringInvoiceService {

    private static final Logger log = LoggerFactory.getLogger(RecurringInvoiceService.class);

    private final RecurringInvoiceRepository recurringRepository;
    private final InvoiceMapper mapper;
    private final ObjectMapper objectMapper;

    public RecurringInvoiceService(RecurringInvoiceRepository recurringRepository,
                                    InvoiceMapper mapper,
                                    ObjectMapper objectMapper) {
        this.recurringRepository = recurringRepository;
        this.mapper = mapper;
        this.objectMapper = objectMapper;
    }

    @Transactional(readOnly = true)
    public RecurringInvoiceListResponse list(String businessId, int page, int limit) {
        Pageable pageable = PageRequest.of(page - 1, limit, Sort.by("createdAt").descending());
        Page<RecurringInvoice> pageResult = recurringRepository.findByBusinessId(businessId, pageable);

        return new RecurringInvoiceListResponse(
                pageResult.getContent().stream().map(mapper::toRecurringResponse).toList(),
                pageResult.getTotalElements(),
                page,
                limit,
                pageResult.getTotalPages()
        );
    }

    @Transactional(readOnly = true)
    public RecurringInvoiceResponse getById(String id, String businessId) {
        RecurringInvoice schedule = recurringRepository.findByIdAndBusinessId(id, businessId)
                .orElseThrow(() -> new ResourceNotFoundException("Recurring invoice not found: " + id));
        return mapper.toRecurringResponse(schedule);
    }

    /**
     * Creates a new recurring invoice schedule from an untyped request map.
     * The frontend sends a generic map because the recurring invoice schema
     * is not yet formalized in TypeScript.
     */
    @Transactional
    public RecurringInvoiceResponse create(Map<String, Object> data) {
        RecurringInvoice schedule = mapToEntity(data, new RecurringInvoice());
        schedule.setCreatedBy(getCurrentUserEmail());
        RecurringInvoice saved = recurringRepository.save(schedule);
        log.info("Created recurring invoice '{}' for business {}", saved.getName(), saved.getBusinessId());
        return mapper.toRecurringResponse(saved);
    }

    @Transactional
    public RecurringInvoiceResponse update(String id, Map<String, Object> data) {
        String businessId = (String) data.get("businessId");
        RecurringInvoice schedule = recurringRepository.findByIdAndBusinessId(id, businessId)
                .orElseThrow(() -> new ResourceNotFoundException("Recurring invoice not found: " + id));

        mapToEntity(data, schedule);
        schedule.setUpdatedAt(Instant.now());
        return mapper.toRecurringResponse(recurringRepository.save(schedule));
    }

    @Transactional
    public void delete(String id, String businessId) {
        RecurringInvoice schedule = recurringRepository.findByIdAndBusinessId(id, businessId)
                .orElseThrow(() -> new ResourceNotFoundException("Recurring invoice not found: " + id));
        recurringRepository.delete(schedule);
    }

    // ── Internal helpers ──────────────────────────────────────────────────────

    /**
     * Maps a generic request map (from frontend) to a RecurringInvoice entity.
     * Fields not present in the map are left unchanged (supports both create and update).
     */
    private RecurringInvoice mapToEntity(Map<String, Object> data, RecurringInvoice entity) {
        if (data.containsKey("businessId"))
            entity.setBusinessId((String) data.get("businessId"));
        if (data.containsKey("name"))
            entity.setName((String) data.get("name"));
        if (data.containsKey("frequency")) {
            String freq = ((String) data.get("frequency")).toUpperCase();
            entity.setFrequency(RecurringFrequency.valueOf(freq));
        }
        if (data.containsKey("status"))
            entity.setStatus((String) data.get("status"));
        if (data.containsKey("startDate"))
            entity.setStartDate(LocalDate.parse((String) data.get("startDate")));
        if (data.containsKey("endCondition"))
            entity.setEndCondition((String) data.get("endCondition"));
        if (data.containsKey("maxOccurrences") && data.get("maxOccurrences") instanceof Number n)
            entity.setMaxOccurrences(n.intValue());
        if (data.containsKey("endDate") && data.get("endDate") != null)
            entity.setEndDate(LocalDate.parse((String) data.get("endDate")));
        if (data.containsKey("currency"))
            entity.setCurrency((String) data.get("currency"));
        if (data.containsKey("dueDaysFromIssue") && data.get("dueDaysFromIssue") instanceof Number n)
            entity.setDueDaysFromIssue(n.intValue());
        if (data.containsKey("description"))
            entity.setDescription((String) data.get("description"));
        if (data.containsKey("paymentTerms"))
            entity.setPaymentTerms((String) data.get("paymentTerms"));
        if (data.containsKey("autoIssue") && data.get("autoIssue") instanceof Boolean b)
            entity.setAutoIssue(b);

        // Recipient
        if (data.containsKey("recipient") && data.get("recipient") instanceof Map<?,?> r) {
            @SuppressWarnings("unchecked")
            Map<String, Object> rMap = (Map<String, Object>) r;
            if (rMap.containsKey("type"))
                entity.setRecipientType(RecipientType.valueOf((String) rMap.get("type")));
            if (rMap.containsKey("email"))
                entity.setRecipientEmail((String) rMap.get("email"));
            if (rMap.containsKey("displayName"))
                entity.setRecipientDisplayName((String) rMap.get("displayName"));
            if (rMap.containsKey("platformId"))
                entity.setRecipientPlatformId((String) rMap.get("platformId"));
        }

        // Line items — serialize to JSON for storage
        if (data.containsKey("lineItems")) {
            try {
                String json = objectMapper.writeValueAsString(data.get("lineItems"));
                entity.setLineItemsJson(json);

                // Recompute totalAmount from line items
                @SuppressWarnings("unchecked")
                List<Map<String, Object>> items = (List<Map<String, Object>>) data.get("lineItems");
                BigDecimal total = items.stream()
                        .map(item -> {
                            Object qty = item.get("quantity");
                            Object price = item.get("unitPrice");
                            if (qty instanceof Number q && price instanceof Number p) {
                                return new BigDecimal(q.toString()).multiply(new BigDecimal(p.toString()));
                            }
                            return BigDecimal.ZERO;
                        })
                        .reduce(BigDecimal.ZERO, BigDecimal::add);
                entity.setTotalAmount(total);
            } catch (Exception e) {
                log.warn("Could not serialize line items: {}", e.getMessage());
            }
        }

        return entity;
    }

    private String getCurrentUserEmail() {
        try {
            return SecurityContextHolder.getContext().getAuthentication().getName();
        } catch (Exception e) {
            return "SYSTEM";
        }
    }
}
