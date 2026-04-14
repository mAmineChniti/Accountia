package com.accountia.invoice.service;

import com.accountia.invoice.domain.entity.Invoice;
import com.accountia.invoice.domain.entity.InvoiceLineItem;
import com.accountia.invoice.domain.entity.InvoiceReceipt;
import com.accountia.invoice.domain.entity.InvoiceRecipient;
import com.accountia.invoice.domain.entity.InvoiceStatusHistory;
import com.accountia.invoice.domain.enums.InvoiceStatus;
import com.accountia.invoice.domain.enums.RecipientType;
import com.accountia.invoice.dto.request.CreateInvoiceRequest;
import com.accountia.invoice.dto.request.TransitionInvoiceRequest;
import com.accountia.invoice.dto.request.UpdateInvoiceRequest;
import com.accountia.invoice.dto.response.InvoiceListResponse;
import com.accountia.invoice.dto.response.InvoiceResponse;
import com.accountia.invoice.exception.InvoiceNotFoundException;
import com.accountia.invoice.exception.InvalidStatusTransitionException;
import com.accountia.invoice.repository.InvoiceReceiptRepository;
import com.accountia.invoice.repository.InvoiceRepository;
import com.accountia.invoice.repository.InvoiceStatusHistoryRepository;
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
import java.util.ArrayList;
import java.util.List;

/**
 * Core business logic for invoice lifecycle management.
 *
 * <p>Transaction strategy:
 * <ul>
 *   <li>All write methods: {@code @Transactional} (default read-write)</li>
 *   <li>All read methods: {@code @Transactional(readOnly = true)} — tells Hibernate to skip
 *       dirty-checking, and tells the DB driver to use a read replica if available.</li>
 * </ul>
 *
 * <p>Constructor injection (not field injection) is used throughout —
 * this makes dependencies explicit and allows easy unit testing with mocks.
 */
@Service
public class InvoiceService {

    private static final Logger log = LoggerFactory.getLogger(InvoiceService.class);

    private final InvoiceRepository invoiceRepository;
    private final InvoiceReceiptRepository receiptRepository;
    private final InvoiceStatusHistoryRepository historyRepository;
    private final InvoiceNumberGenerator numberGenerator;
    private final InvoiceMapper mapper;

    public InvoiceService(InvoiceRepository invoiceRepository,
                          InvoiceReceiptRepository receiptRepository,
                          InvoiceStatusHistoryRepository historyRepository,
                          InvoiceNumberGenerator numberGenerator,
                          InvoiceMapper mapper) {
        this.invoiceRepository = invoiceRepository;
        this.receiptRepository = receiptRepository;
        this.historyRepository = historyRepository;
        this.numberGenerator = numberGenerator;
        this.mapper = mapper;
    }

    // ── CREATE ────────────────────────────────────────────────────────────────

    /**
     * Creates a new invoice in DRAFT status.
     *
     * <p>Steps:
     * <ol>
     *   <li>Generate a unique sequential invoice number.</li>
     *   <li>Map line items and compute amounts (quantity × unitPrice).</li>
     *   <li>Sum all line item amounts to get totalAmount.</li>
     *   <li>Save the invoice.</li>
     *   <li>Record the initial status in the audit trail.</li>
     *   <li>If recipient is a platform user, create a receipt in their inbox.</li>
     * </ol>
     */
    @Transactional
    public InvoiceResponse createInvoice(CreateInvoiceRequest request) {
        // 1. Validate business rule: dueDate must be after issuedDate
        if (request.dueDate().isBefore(request.issuedDate())) {
            throw new IllegalArgumentException("Due date must be on or after issued date");
        }

        // 2. Validate EXTERNAL recipient has a display name
        if (request.recipient().type() == RecipientType.EXTERNAL
                && (request.recipient().displayName() == null || request.recipient().displayName().isBlank())) {
            throw new IllegalArgumentException("Display name is required for EXTERNAL recipients");
        }

        // 3. Generate invoice number (sequential per business per year)
        String invoiceNumber = numberGenerator.generate(request.businessId());

        // 4. Map line items, computing amount = quantity × unitPrice for each
        List<InvoiceLineItem> lineItems = new ArrayList<>();
        for (int i = 0; i < request.lineItems().size(); i++) {
            lineItems.add(mapper.toLineItemEntity(request.lineItems().get(i), i));
        }

        // 5. Sum all line item amounts to get the invoice total
        BigDecimal totalAmount = lineItems.stream()
                .map(InvoiceLineItem::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        // 6. Build the invoice entity
        InvoiceRecipient recipient = mapper.toRecipientEntity(request.recipient());
        Invoice invoice = Invoice.builder()
                .issuerBusinessId(request.businessId())
                .invoiceNumber(invoiceNumber)
                .status(InvoiceStatus.DRAFT)
                .totalAmount(totalAmount)
                .amountPaid(BigDecimal.ZERO)
                .currency(request.currency())
                .issuedDate(request.issuedDate())
                .dueDate(request.dueDate())
                .description(request.description())
                .paymentTerms(request.paymentTerms())
                .recipient(recipient)
                .build();

        // Wire the line items to the invoice (sets the FK invoice_id)
        lineItems.forEach(invoice::addLineItem);

        Invoice saved = invoiceRepository.save(invoice);
        log.info("Created invoice {} for business {}", invoiceNumber, request.businessId());

        // 7. Record the initial DRAFT status in the audit trail
        recordStatusHistory(saved, null, InvoiceStatus.DRAFT, null);

        return mapper.toResponse(saved);
    }

    // ── LIST (paginated) ──────────────────────────────────────────────────────

    /**
     * Lists issued invoices with optional status filter and pagination.
     *
     * <p>Frontend sends page=1 for the first page (1-based).
     * Spring Data uses 0-based pages, so we convert: {@code SpringPage = frontendPage - 1}.
     *
     * @param businessId the issuing business UUID
     * @param status     optional filter (null = all statuses)
     * @param page       1-based page number from frontend
     * @param limit      page size
     */
    @Transactional(readOnly = true)
    public InvoiceListResponse listIssuedInvoices(String businessId, InvoiceStatus status,
                                                   int page, int limit) {
        // Convert 1-based frontend page to 0-based Spring Data page
        Pageable pageable = PageRequest.of(page - 1, limit, Sort.by("createdAt").descending());

        Page<Invoice> pageResult = invoiceRepository.findByIssuerBusinessIdAndStatus(
                businessId, status, pageable);

        long grandTotal = invoiceRepository.countByIssuerBusinessId(businessId);

        return new InvoiceListResponse(
                pageResult.getContent().stream().map(mapper::toResponse).toList(),
                grandTotal,
                pageResult.getTotalElements(),   // filtered total
                page,                            // return the 1-based page as-is
                limit,
                pageResult.getTotalPages()
        );
    }

    // ── GET SINGLE ────────────────────────────────────────────────────────────

    /**
     * Fetches a single invoice with all its line items (JOIN FETCH in repository).
     * Also marks the corresponding receipt as "viewed" if one exists.
     *
     * @param id         invoice UUID
     * @param businessId must match the invoice's issuer business (authorization)
     */
    @Transactional
    public InvoiceResponse getIssuedInvoice(String id, String businessId) {
        Invoice invoice = invoiceRepository.findByIdAndBusinessIdWithItems(id, businessId)
                .orElseThrow(() -> new InvoiceNotFoundException(id));
        return mapper.toResponse(invoice);
    }

    // ── UPDATE (PATCH) ────────────────────────────────────────────────────────

    /**
     * Updates editable fields on a DRAFT invoice.
     *
     * <p>Only description, paymentTerms, and dueDate can be changed.
     * The invoice must still be in DRAFT status — issued invoices are immutable.
     * Null fields in the request are ignored (partial update / PATCH semantics).
     */
    @Transactional
    public InvoiceResponse updateInvoice(String id, UpdateInvoiceRequest request) {
        Invoice invoice = invoiceRepository.findByIdAndBusinessIdWithItems(id, request.businessId())
                .orElseThrow(() -> new InvoiceNotFoundException(id));

        if (invoice.getStatus() != InvoiceStatus.DRAFT) {
            throw new IllegalArgumentException(
                    "Only DRAFT invoices can be edited. Current status: " + invoice.getStatus());
        }

        // Apply only non-null fields (PATCH semantics)
        if (request.description() != null) {
            invoice.setDescription(request.description());
        }
        if (request.paymentTerms() != null) {
            invoice.setPaymentTerms(request.paymentTerms());
        }
        if (request.dueDate() != null) {
            if (request.dueDate().isBefore(invoice.getIssuedDate())) {
                throw new IllegalArgumentException("Due date must be on or after issued date");
            }
            invoice.setDueDate(request.dueDate());
        }

        return mapper.toResponse(invoiceRepository.save(invoice));
    }

    // ── TRANSITION (STATE MACHINE) ────────────────────────────────────────────

    /**
     * Transitions an invoice to a new status.
     *
     * <p>Validation:
     * <ol>
     *   <li>The requested transition must be allowed by {@link InvoiceStatus#canTransitionTo}.</li>
     *   <li>If transitioning to PARTIAL: amountPaid must be provided and less than totalAmount.</li>
     *   <li>If transitioning to PAID: amountPaid is set to totalAmount automatically.</li>
     * </ol>
     *
     * <p>Side effects:
     * <ul>
     *   <li>Status history entry is recorded.</li>
     *   <li>All receipts for this invoice are synced to the new status.</li>
     * </ul>
     */
    @Transactional
    public InvoiceResponse transitionInvoice(String id, TransitionInvoiceRequest request) {
        Invoice invoice = invoiceRepository.findByIdAndBusinessIdWithItems(id, request.businessId())
                .orElseThrow(() -> new InvoiceNotFoundException(id));

        InvoiceStatus currentStatus = invoice.getStatus();
        InvoiceStatus targetStatus = request.newStatus();

        // Validate the transition using the state machine defined on the enum
        if (!currentStatus.canTransitionTo(targetStatus)) {
            throw new InvalidStatusTransitionException(currentStatus, targetStatus);
        }

        // Business rules for payment transitions
        if (targetStatus == InvoiceStatus.PARTIAL) {
            if (request.amountPaid() == null || request.amountPaid().compareTo(BigDecimal.ZERO) <= 0) {
                throw new IllegalArgumentException("amountPaid must be provided for PARTIAL status");
            }
            if (request.amountPaid().compareTo(invoice.getTotalAmount()) >= 0) {
                throw new IllegalArgumentException(
                        "For PARTIAL status, amountPaid must be less than totalAmount. Use PAID status for full payment.");
            }
            invoice.setAmountPaid(request.amountPaid());
        }

        if (targetStatus == InvoiceStatus.PAID) {
            // If amountPaid was provided, use it; otherwise default to totalAmount
            invoice.setAmountPaid(request.amountPaid() != null
                    ? request.amountPaid()
                    : invoice.getTotalAmount());
        }

        // Apply the transition
        invoice.setStatus(targetStatus);
        invoice.setLastStatusChangeAt(Instant.now());
        Invoice saved = invoiceRepository.save(invoice);

        // Record audit trail
        recordStatusHistory(saved, currentStatus, targetStatus, request.reason());

        // Sync all receipts to the new status (so recipients see up-to-date status)
        syncReceiptsStatus(saved.getId(), targetStatus);

        log.info("Invoice {} transitioned: {} → {} by {}",
                invoice.getInvoiceNumber(), currentStatus, targetStatus, getCurrentUserEmail());

        return mapper.toResponse(saved);
    }

    // ── SOFT DELETE ───────────────────────────────────────────────────────────

    /**
     * Soft-deletes an invoice by setting isDeleted=true.
     * The {@code @SQLRestriction} on the entity ensures it becomes invisible to all queries.
     */
    @Transactional
    public void deleteInvoice(String id, String businessId) {
        Invoice invoice = invoiceRepository.findByIdAndBusinessIdWithItems(id, businessId)
                .orElseThrow(() -> new InvoiceNotFoundException(id));

        invoice.setDeleted(true);
        invoice.setDeletedAt(Instant.now());
        invoiceRepository.save(invoice);

        log.info("Soft-deleted invoice {} for business {}", invoice.getInvoiceNumber(), businessId);
    }

    // ── INTERNAL HELPERS ──────────────────────────────────────────────────────

    /**
     * Creates a new status history entry (called after every transition and on create).
     */
    private void recordStatusHistory(Invoice invoice, InvoiceStatus oldStatus,
                                     InvoiceStatus newStatus, String reason) {
        InvoiceStatusHistory history = InvoiceStatusHistory.builder()
                .invoice(invoice)
                .oldStatus(oldStatus)
                .newStatus(newStatus)
                .changedBy(getCurrentUserEmail())
                .reason(reason)
                .build();
        historyRepository.save(history);
    }

    /**
     * Updates all receipt rows for a given invoice to reflect the new invoice status.
     * Called after every status transition so recipients see the current status.
     */
    private void syncReceiptsStatus(String invoiceId, InvoiceStatus newStatus) {
        List<InvoiceReceipt> receipts = receiptRepository.findAllByInvoiceId(invoiceId);
        receipts.forEach(r -> {
            r.setInvoiceStatus(newStatus);
            r.setLastSyncedAt(Instant.now());
        });
        receiptRepository.saveAll(receipts);
    }

    /**
     * Extracts the current user's email from the Spring Security context.
     * The JWT filter sets this as the principal name.
     */
    private String getCurrentUserEmail() {
        try {
            return SecurityContextHolder.getContext().getAuthentication().getName();
        } catch (Exception e) {
            return "SYSTEM";
        }
    }
}
