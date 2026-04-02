package com.accountia.invoice.service;

import com.accountia.invoice.dto.InvoiceDTO;
import com.accountia.invoice.model.Invoice;
import com.accountia.invoice.repository.InvoiceRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDate;

@Service
public class InvoiceService {
    private final InvoiceRepository invoiceRepository;

    public InvoiceService(InvoiceRepository invoiceRepository) {
        this.invoiceRepository = invoiceRepository;
    }

    public InvoiceDTO createInvoice(InvoiceDTO dto) {
        Invoice invoice = new Invoice();
        invoice.setTenantId(dto.getTenantId());
        invoice.setClientName(dto.getClientName());
        invoice.setAmount(dto.getAmount());
        invoice.setIssueDate(dto.getIssueDate() == null ? LocalDate.now() : dto.getIssueDate());

        Invoice saved = invoiceRepository.save(invoice);
        InvoiceDTO out = new InvoiceDTO();
        out.setId(saved.getId());
        out.setTenantId(saved.getTenantId());
        out.setClientName(saved.getClientName());
        out.setAmount(saved.getAmount());
        out.setIssueDate(saved.getIssueDate());
        return out;
    }
}
