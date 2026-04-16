package com.accountia.invoice.service;

import com.accountia.invoice.dto.InvoiceDTO;
import com.accountia.invoice.model.Invoice;
import com.accountia.invoice.repository.InvoiceRepository;
import com.accountia.invoice.util.SecurityUtil;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Service
public class InvoiceService {

    private final InvoiceRepository invoiceRepository;

    public InvoiceService(InvoiceRepository invoiceRepository) {
        this.invoiceRepository = invoiceRepository;
    }

    // ─── GET ALL ────────────────────────────────────────────────
    public List<Invoice> getAllInvoices() {
        if (SecurityUtil.isAdmin()) {
            return invoiceRepository.findAll();
        }
        String subject = SecurityUtil.getCurrentSubject();
        if (subject == null || subject.isBlank()) return List.of();
        return invoiceRepository.findByOwnerSubject(subject);
    }

    // ─── GET BY ID ──────────────────────────────────────────────
    public Optional<Invoice> getInvoiceById(Long id) {
        return invoiceRepository.findById(id)
                .filter(this::canAccess);
    }

    // ─── SEARCH BY CLIENT NAME ──────────────────────────────────
    public List<Invoice> searchByClientName(String clientName) {
        if (SecurityUtil.isAdmin()) {
            return invoiceRepository.findByClientNameContainingIgnoreCase(clientName);
        }
        String subject = SecurityUtil.getCurrentSubject();
        if (subject == null || subject.isBlank()) return List.of();
        return invoiceRepository.findByOwnerSubjectAndClientNameContainingIgnoreCase(subject, clientName);
    }

    // ─── FILTER BY STATUS ───────────────────────────────────────
    public List<Invoice> getByStatus(String status) {
        if (SecurityUtil.isAdmin()) {
            return invoiceRepository.findByStatus(status);
        }
        String subject = SecurityUtil.getCurrentSubject();
        if (subject == null || subject.isBlank()) return List.of();
        return invoiceRepository.findByOwnerSubjectAndStatus(subject, status);
    }

    // ─── CREATE ──────────────────────────────────────────────────
    public Invoice createInvoice(InvoiceDTO dto) {
        Invoice invoice = new Invoice();
        invoice.setTenantId(dto.getTenantId());
        invoice.setClientName(dto.getClientName());
        invoice.setAmount(dto.getAmount());
        invoice.setDescription(dto.getDescription());
        invoice.setIssueDate(dto.getIssueDate() == null ? LocalDate.now() : dto.getIssueDate());
        invoice.setDueDate(dto.getDueDate());
        invoice.setStatus(dto.getStatus() == null ? "DRAFT" : dto.getStatus());
        invoice.setOwnerSubject(SecurityUtil.getCurrentSubject());
        return invoiceRepository.save(invoice);
    }

    // ─── UPDATE ──────────────────────────────────────────────────
    public Invoice updateInvoice(Long id, InvoiceDTO dto) {
        return invoiceRepository.findById(id).map(existing -> {
            ensureOwnershipOrAdmin(existing);
            existing.setClientName(dto.getClientName());
            existing.setAmount(dto.getAmount());
            existing.setDescription(dto.getDescription());
            existing.setDueDate(dto.getDueDate());
            if (dto.getStatus() != null) existing.setStatus(dto.getStatus());
            return invoiceRepository.save(existing);
        }).orElse(null);
    }

    // ─── DELETE ──────────────────────────────────────────────────
    public String deleteInvoice(Long id) {
        Optional<Invoice> existing = invoiceRepository.findById(id);
        if (existing.isPresent()) {
            ensureOwnershipOrAdmin(existing.get());
            invoiceRepository.deleteById(id);
            return "Facture supprimée avec succès";
        }
        return "Facture introuvable";
    }

    // ─── HELPERS ─────────────────────────────────────────────────
    private void ensureOwnershipOrAdmin(Invoice invoice) {
        if (!canAccess(invoice)) {
            throw new AccessDeniedException("Accès refusé : vous ne pouvez modifier que vos propres factures");
        }
    }

    private boolean canAccess(Invoice invoice) {
        if (SecurityUtil.isAdmin()) return true;
        String subject = SecurityUtil.getCurrentSubject();
        return subject != null && subject.equals(invoice.getOwnerSubject());
    }
}
