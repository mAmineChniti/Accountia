package com.accountia.invoice.service;

import com.accountia.invoice.dto.InvoiceDTO;
import com.accountia.invoice.exception.ResourceNotFoundException;
import com.accountia.invoice.model.Invoice;
import com.accountia.invoice.repository.InvoiceRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class InvoiceService {
    private final InvoiceRepository invoiceRepository;

    public InvoiceService(InvoiceRepository invoiceRepository) {
        this.invoiceRepository = invoiceRepository;
    }

    // GET ALL
    public List<InvoiceDTO> getAllInvoices() {
        return invoiceRepository.findAll()
                .stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    // GET BY ID
    public InvoiceDTO getInvoiceById(Long id) {
        Invoice invoice = invoiceRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Facture introuvable avec id: " + id));
        return toDTO(invoice);
    }

    // GET BY CLIENT ID
    public List<InvoiceDTO> getByClientId(Long clientId) {
        return invoiceRepository.findByClientId(clientId)
                .stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    // GET BY BUSINESS ID
    public List<InvoiceDTO> getByBusinessId(Long businessId) {
        return invoiceRepository.findByBusinessId(businessId)
                .stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    // GET BY STATUS
    public List<InvoiceDTO> getByStatus(String status) {
        Invoice.InvoiceStatus invoiceStatus = Invoice.InvoiceStatus.valueOf(status.toUpperCase());
        return invoiceRepository.findByStatus(invoiceStatus)
                .stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    // SEARCH BY CLIENT NAME
    public List<InvoiceDTO> searchByClientName(String clientName) {
        return invoiceRepository.findByClientNameContainingIgnoreCase(clientName)
                .stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    // CREATE
    public InvoiceDTO createInvoice(InvoiceDTO dto) {
        Invoice invoice = toEntity(dto);
        if (invoice.getIssueDate() == null) {
            invoice.setIssueDate(LocalDate.now());
        }
        if (invoice.getStatus() == null) {
            invoice.setStatus(Invoice.InvoiceStatus.DRAFT);
        }
        Invoice saved = invoiceRepository.save(invoice);
        return toDTO(saved);
    }

    // UPDATE
    public InvoiceDTO updateInvoice(Long id, InvoiceDTO dto) {
        Invoice existing = invoiceRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Facture introuvable avec id: " + id));

        existing.setInvoiceNumber(dto.getInvoiceNumber());
        existing.setTenantId(dto.getTenantId());
        existing.setClientName(dto.getClientName());
        existing.setDescription(dto.getDescription());
        existing.setAmount(dto.getAmount());
        existing.setTaxAmount(dto.getTaxAmount());
        existing.setTotalAmount(dto.getTotalAmount());
        existing.setIssueDate(dto.getIssueDate());
        existing.setDueDate(dto.getDueDate());
        if (dto.getStatus() != null) {
            existing.setStatus(Invoice.InvoiceStatus.valueOf(dto.getStatus().toUpperCase()));
        }
        existing.setBusinessId(dto.getBusinessId());
        existing.setClientId(dto.getClientId());

        Invoice saved = invoiceRepository.save(existing);
        return toDTO(saved);
    }

    // DELETE
    public String deleteInvoice(Long id) {
        if (invoiceRepository.existsById(id)) {
            invoiceRepository.deleteById(id);
            return "Facture supprimée avec succès";
        }
        throw new ResourceNotFoundException("Facture introuvable avec id: " + id);
    }

    // ---- Mapping helpers ----

    private InvoiceDTO toDTO(Invoice invoice) {
        InvoiceDTO dto = new InvoiceDTO();
        dto.setId(invoice.getId());
        dto.setInvoiceNumber(invoice.getInvoiceNumber());
        dto.setTenantId(invoice.getTenantId());
        dto.setClientName(invoice.getClientName());
        dto.setDescription(invoice.getDescription());
        dto.setAmount(invoice.getAmount());
        dto.setTaxAmount(invoice.getTaxAmount());
        dto.setTotalAmount(invoice.getTotalAmount());
        dto.setIssueDate(invoice.getIssueDate());
        dto.setDueDate(invoice.getDueDate());
        dto.setStatus(invoice.getStatus() != null ? invoice.getStatus().name() : null);
        dto.setBusinessId(invoice.getBusinessId());
        dto.setClientId(invoice.getClientId());
        return dto;
    }

    private Invoice toEntity(InvoiceDTO dto) {
        Invoice invoice = new Invoice();
        invoice.setInvoiceNumber(dto.getInvoiceNumber());
        invoice.setTenantId(dto.getTenantId());
        invoice.setClientName(dto.getClientName());
        invoice.setDescription(dto.getDescription());
        invoice.setAmount(dto.getAmount());
        invoice.setTaxAmount(dto.getTaxAmount());
        invoice.setTotalAmount(dto.getTotalAmount());
        invoice.setIssueDate(dto.getIssueDate());
        invoice.setDueDate(dto.getDueDate());
        if (dto.getStatus() != null) {
            invoice.setStatus(Invoice.InvoiceStatus.valueOf(dto.getStatus().toUpperCase()));
        }
        invoice.setBusinessId(dto.getBusinessId());
        invoice.setClientId(dto.getClientId());
        return invoice;
    }
}
