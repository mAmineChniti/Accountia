package com.accountia.invoice.service;

import com.accountia.invoice.repository.InvoiceRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;

/**
 * Generates sequential, human-readable invoice numbers per business per year.
 *
 * <p>Format: {@code INV-{YEAR}-{5-digit-sequence}}
 * Example: {@code INV-2026-00001}, {@code INV-2026-00002}, ...
 *
 * <p>Sequence resets each year per business. Each business has its own sequence.
 *
 * <p>Thread safety: The method is {@code @Transactional} to prevent two concurrent
 * invoice creations from getting the same number. The query uses MAX() which is
 * safe under MySQL's row-level locking.
 */
@Service
public class InvoiceNumberGenerator {

    private final InvoiceRepository invoiceRepository;

    public InvoiceNumberGenerator(InvoiceRepository invoiceRepository) {
        this.invoiceRepository = invoiceRepository;
    }

    /**
     * Generates the next invoice number for the given business and current year.
     *
     * <p>Algorithm:
     * <ol>
     *   <li>Query MAX(invoiceNumber) WHERE invoiceNumber LIKE 'INV-{year}-%'</li>
     *   <li>Parse the sequence part from the max (e.g. "INV-2026-00005" → 5)</li>
     *   <li>Return "INV-{year}-{(max+1) padded to 5 digits}"</li>
     * </ol>
     *
     * @param businessId the issuing business UUID
     * @return a unique invoice number like {@code INV-2026-00001}
     */
    @Transactional
    public String generate(String businessId) {
        int year = LocalDate.now().getYear();
        String pattern = "INV-" + year + "-%";   // SQL LIKE pattern for this year

        String maxNumber = invoiceRepository
                .findMaxInvoiceNumberForPattern(businessId, pattern)
                .orElse(null);

        int nextSequence;
        if (maxNumber == null) {
            // First invoice of the year for this business
            nextSequence = 1;
        } else {
            // Parse the sequence: "INV-2026-00005" → split on "-" → take last part → parse int
            String[] parts = maxNumber.split("-");
            nextSequence = Integer.parseInt(parts[parts.length - 1]) + 1;
        }

        // Pad to 5 digits: 1 → "00001", 99999 → "99999"
        return String.format("INV-%d-%05d", year, nextSequence);
    }
}
