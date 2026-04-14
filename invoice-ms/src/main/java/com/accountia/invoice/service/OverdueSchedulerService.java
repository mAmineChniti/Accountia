package com.accountia.invoice.service;

import com.accountia.invoice.domain.entity.Invoice;
import com.accountia.invoice.domain.entity.InvoiceStatusHistory;
import com.accountia.invoice.domain.enums.InvoiceStatus;
import com.accountia.invoice.repository.InvoiceRepository;
import com.accountia.invoice.repository.InvoiceStatusHistoryRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.LocalDate;
import java.util.List;

/**
 * Nightly scheduled job that automatically marks overdue invoices.
 *
 * <p>An invoice is considered overdue when:
 * <ul>
 *   <li>Its status is {@code ISSUED} or {@code VIEWED} (pending payment).</li>
 *   <li>Its {@code dueDate} is before today.</li>
 * </ul>
 *
 * <p>The cron expression {@code "0 0 1 * * *"} means:
 * second=0, minute=0, hour=1 (1:00 AM), every day of month, every month, every day of week.
 * So the job runs every day at 01:00 AM server time.
 *
 * <p>Note: {@code @EnableScheduling} in InvoiceApplication activates this scheduler.
 */
@Service
public class OverdueSchedulerService {

    private static final Logger log = LoggerFactory.getLogger(OverdueSchedulerService.class);

    private final InvoiceRepository invoiceRepository;
    private final InvoiceStatusHistoryRepository historyRepository;

    public OverdueSchedulerService(InvoiceRepository invoiceRepository,
                                    InvoiceStatusHistoryRepository historyRepository) {
        this.invoiceRepository = invoiceRepository;
        this.historyRepository = historyRepository;
    }

    /**
     * Runs every day at 01:00 AM.
     * Finds all ISSUED/VIEWED invoices past their due date and marks them OVERDUE.
     *
     * <p>{@code @Transactional} ensures that if the batch update fails partway through,
     * no partial changes are committed.
     */
    @Scheduled(cron = "0 0 1 * * *")
    @Transactional
    public void markOverdueInvoices() {
        LocalDate today = LocalDate.now();

        // The statuses that can become overdue (invoices that haven't been paid yet)
        List<InvoiceStatus> checkStatuses = List.of(InvoiceStatus.ISSUED, InvoiceStatus.VIEWED);

        List<Invoice> overdueInvoices = invoiceRepository.findOverdueInvoices(checkStatuses, today);

        if (overdueInvoices.isEmpty()) {
            log.debug("Overdue check: no invoices to mark as OVERDUE");
            return;
        }

        log.info("Overdue check: marking {} invoice(s) as OVERDUE", overdueInvoices.size());

        for (Invoice invoice : overdueInvoices) {
            InvoiceStatus previousStatus = invoice.getStatus();
            invoice.setStatus(InvoiceStatus.OVERDUE);
            invoice.setLastStatusChangeAt(Instant.now());

            // Record the automated status change in audit trail
            // "SYSTEM" is used as the changedBy value to indicate this was automated
            InvoiceStatusHistory history = InvoiceStatusHistory.builder()
                    .invoice(invoice)
                    .oldStatus(previousStatus)
                    .newStatus(InvoiceStatus.OVERDUE)
                    .changedBy("SYSTEM")
                    .reason("Invoice past due date — marked overdue automatically by scheduler")
                    .build();

            historyRepository.save(history);

            log.info("Invoice {} [{}] marked OVERDUE (was {})",
                    invoice.getInvoiceNumber(), invoice.getId(), previousStatus);
        }

        // Save all modified invoices in one batch
        invoiceRepository.saveAll(overdueInvoices);
    }
}
