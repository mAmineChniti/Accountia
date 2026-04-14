package com.accountia.invoice.service;

import com.accountia.invoice.domain.entity.Invoice;
import com.accountia.invoice.domain.entity.InvoiceReceipt;
import com.accountia.invoice.domain.enums.InvoiceStatus;
import com.accountia.invoice.dto.response.InvoiceResponse;
import com.accountia.invoice.dto.response.InvoiceReceiptResponse;
import com.accountia.invoice.dto.response.ReceivedInvoiceListResponse;
import com.accountia.invoice.exception.InvoiceNotFoundException;
import com.accountia.invoice.repository.InvoiceReceiptRepository;
import com.accountia.invoice.repository.InvoiceRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;

/**
 * Manages the "received invoices" inbox for platform recipients.
 *
 * <p>When a PLATFORM_BUSINESS or PLATFORM_INDIVIDUAL invoice is issued,
 * InvoiceService calls {@link #createReceiptForPlatformRecipient} to add
 * an entry to the recipient's inbox. This service handles querying that inbox.
 */
@Service
public class InvoiceReceiptService {

    private static final Logger log = LoggerFactory.getLogger(InvoiceReceiptService.class);

    private final InvoiceReceiptRepository receiptRepository;
    private final InvoiceRepository invoiceRepository;
    private final InvoiceMapper mapper;

    public InvoiceReceiptService(InvoiceReceiptRepository receiptRepository,
                                  InvoiceRepository invoiceRepository,
                                  InvoiceMapper mapper) {
        this.receiptRepository = receiptRepository;
        this.invoiceRepository = invoiceRepository;
        this.mapper = mapper;
    }

    // ── Business recipient inbox ──────────────────────────────────────────────

    @Transactional(readOnly = true)
    public ReceivedInvoiceListResponse listByBusiness(String businessId, InvoiceStatus status,
                                                       int page, int limit) {
        Pageable pageable = PageRequest.of(page - 1, limit, Sort.by("createdAt").descending());
        Page<InvoiceReceipt> pageResult = receiptRepository.findByRecipientBusinessId(
                businessId, status, pageable);
        long grandTotal = receiptRepository.countByRecipientBusinessId(businessId);

        return new ReceivedInvoiceListResponse(
                pageResult.getContent().stream().map(mapper::toReceiptResponse).toList(),
                grandTotal,
                pageResult.getTotalElements(),
                page,
                limit,
                pageResult.getTotalPages()
        );
    }

    @Transactional
    public InvoiceResponse getReceiptDetailsByBusiness(String receiptId, String businessId) {
        InvoiceReceipt receipt = receiptRepository.findByIdAndRecipientBusinessId(receiptId, businessId)
                .orElseThrow(() -> new InvoiceNotFoundException(receiptId));

        markAsViewed(receipt);

        // Load the original invoice (with line items) for the full response
        return invoiceRepository.findByIdAndBusinessIdWithItems(
                        receipt.getInvoice().getId(),
                        receipt.getIssuerBusinessId())
                .map(mapper::toResponse)
                .orElseThrow(() -> new InvoiceNotFoundException(receipt.getInvoice().getId()));
    }

    // ── Individual recipient inbox ────────────────────────────────────────────

    @Transactional(readOnly = true)
    public ReceivedInvoiceListResponse listByIndividual(String userId, InvoiceStatus status,
                                                         int page, int limit) {
        Pageable pageable = PageRequest.of(page - 1, limit, Sort.by("createdAt").descending());
        Page<InvoiceReceipt> pageResult = receiptRepository.findByRecipientUserId(
                userId, status, pageable);
        long grandTotal = receiptRepository.countByRecipientUserId(userId);

        return new ReceivedInvoiceListResponse(
                pageResult.getContent().stream().map(mapper::toReceiptResponse).toList(),
                grandTotal,
                pageResult.getTotalElements(),
                page,
                limit,
                pageResult.getTotalPages()
        );
    }

    @Transactional
    public InvoiceResponse getReceiptDetailsByIndividual(String receiptId, String userId) {
        InvoiceReceipt receipt = receiptRepository.findByIdAndRecipientUserId(receiptId, userId)
                .orElseThrow(() -> new InvoiceNotFoundException(receiptId));

        markAsViewed(receipt);

        return invoiceRepository.findByIdAndBusinessIdWithItems(
                        receipt.getInvoice().getId(),
                        receipt.getIssuerBusinessId())
                .map(mapper::toResponse)
                .orElseThrow(() -> new InvoiceNotFoundException(receipt.getInvoice().getId()));
    }

    // ── Receipt creation (called by InvoiceService on issue) ─────────────────

    /**
     * Creates an InvoiceReceipt for a platform recipient when an invoice is issued.
     * Called from InvoiceService.transitionInvoice when status changes to ISSUED.
     */
    @Transactional
    public void createReceiptForPlatformRecipient(Invoice invoice) {
        var recipient = invoice.getRecipient();
        if (recipient == null) return;

        // Only create receipts for platform (not external) recipients
        if (recipient.getType() == null || recipient.getType().name().equals("EXTERNAL")) {
            return;
        }

        InvoiceReceipt receipt = InvoiceReceipt.builder()
                .invoice(invoice)
                .issuerTenantDatabaseName(invoice.getIssuerBusinessId()) // placeholder until tenant resolution
                .issuerBusinessId(invoice.getIssuerBusinessId())
                .issuerBusinessName(invoice.getIssuerBusinessId()) // placeholder — real name from business-ms
                .invoiceNumber(invoice.getInvoiceNumber())
                .totalAmount(invoice.getTotalAmount())
                .currency(invoice.getCurrency())
                .issuedDate(invoice.getIssuedDate())
                .dueDate(invoice.getDueDate())
                .invoiceStatus(invoice.getStatus())
                .recipientBusinessId(
                        "PLATFORM_BUSINESS".equals(recipient.getType().name())
                                ? recipient.getPlatformId() : null)
                .recipientUserId(
                        "PLATFORM_INDIVIDUAL".equals(recipient.getType().name())
                                ? recipient.getPlatformId() : null)
                .build();

        receiptRepository.save(receipt);
        log.info("Created receipt for invoice {} for platform recipient {}",
                invoice.getInvoiceNumber(), recipient.getEmail());
    }

    // ── Private helpers ───────────────────────────────────────────────────────

    /** Marks a receipt as viewed (called when the recipient fetches details). */
    private void markAsViewed(InvoiceReceipt receipt) {
        if (!receipt.isRecipientViewed()) {
            receipt.setRecipientViewed(true);
            receipt.setRecipientViewedAt(Instant.now());
            receiptRepository.save(receipt);
        }
    }
}
