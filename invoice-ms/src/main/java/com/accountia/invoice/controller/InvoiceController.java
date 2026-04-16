package com.accountia.invoice.controller;

import com.accountia.invoice.dto.InvoiceDTO;
import com.accountia.invoice.model.Invoice;
import com.accountia.invoice.service.InvoiceService;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/invoice")
@CrossOrigin(origins = "*")
public class InvoiceController {

    private final InvoiceService invoiceService;

    public InvoiceController(InvoiceService invoiceService) {
        this.invoiceService = invoiceService;
    }

    // ─── HEALTH ──────────────────────────────────────────────────
    @GetMapping("/health")
    public ResponseEntity<?> health() {
        return ResponseEntity.ok("invoice-ms up");
    }

    // ─── GET ALL - /api/invoice/invoices ─────────────────────────
    @GetMapping("/invoices")
    public ResponseEntity<List<Invoice>> getAllInvoices() {
        List<Invoice> invoices = invoiceService.getAllInvoices();
        if (invoices.isEmpty()) return ResponseEntity.noContent().build();
        return ResponseEntity.ok(invoices);
    }

    // ─── GET BY ID - /api/invoice/invoices/1 ─────────────────────
    @GetMapping("/invoices/{id}")
    public ResponseEntity<Invoice> getInvoiceById(@PathVariable Long id) {
        return invoiceService.getInvoiceById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    // ─── SEARCH BY CLIENT NAME - /api/invoice/invoices/search?clientName=Ben
    @GetMapping("/invoices/search")
    public ResponseEntity<List<Invoice>> searchByClientName(@RequestParam String clientName) {
        List<Invoice> invoices = invoiceService.searchByClientName(clientName);
        if (invoices.isEmpty()) return ResponseEntity.noContent().build();
        return ResponseEntity.ok(invoices);
    }

    // ─── FILTER BY STATUS - /api/invoice/invoices/status?status=PAID
    @GetMapping("/invoices/status")
    public ResponseEntity<List<Invoice>> getByStatus(@RequestParam String status) {
        List<Invoice> invoices = invoiceService.getByStatus(status);
        if (invoices.isEmpty()) return ResponseEntity.noContent().build();
        return ResponseEntity.ok(invoices);
    }

    // ─── POST - Créer une facture ─────────────────────────────────
    @PostMapping(value = "/invoices", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Invoice> createInvoice(@RequestBody InvoiceDTO invoiceDTO) {
        return new ResponseEntity<>(invoiceService.createInvoice(invoiceDTO), HttpStatus.CREATED);
    }

    // ─── PUT - Modifier une facture ───────────────────────────────
    @PutMapping("/invoices/{id}")
    public ResponseEntity<Invoice> updateInvoice(@PathVariable Long id,
                                                  @RequestBody InvoiceDTO invoiceDTO) {
        Invoice updated = invoiceService.updateInvoice(id, invoiceDTO);
        if (updated == null) return ResponseEntity.notFound().build();
        return ResponseEntity.ok(updated);
    }

    // ─── DELETE - Supprimer une facture ───────────────────────────
    @DeleteMapping("/invoices/{id}")
    public ResponseEntity<String> deleteInvoice(@PathVariable Long id) {
        return new ResponseEntity<>(invoiceService.deleteInvoice(id), HttpStatus.OK);
    }
}
