package com.accountia.invoice.controller;

import com.accountia.invoice.dto.InvoiceDTO;
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

    @GetMapping("/health")
    public ResponseEntity<?> health() {
        return ResponseEntity.ok("invoice-ms up");
    }

    // GET ALL - /api/invoice/invoices
    @GetMapping("/invoices")
    public ResponseEntity<List<InvoiceDTO>> getAllInvoices() {
        List<InvoiceDTO> invoices = invoiceService.getAllInvoices();
        if (invoices.isEmpty()) return ResponseEntity.noContent().build();
        return ResponseEntity.ok(invoices);
    }

    // GET BY ID - /api/invoice/invoices/1
    @GetMapping("/invoices/{id}")
    public ResponseEntity<InvoiceDTO> getInvoiceById(@PathVariable Long id) {
        return ResponseEntity.ok(invoiceService.getInvoiceById(id));
    }

    // GET BY CLIENT ID - /api/invoice/invoices/client/1
    @GetMapping("/invoices/client/{clientId}")
    public ResponseEntity<List<InvoiceDTO>> getByClientId(@PathVariable Long clientId) {
        return ResponseEntity.ok(invoiceService.getByClientId(clientId));
    }

    // GET BY BUSINESS ID - /api/invoice/invoices/business/1
    @GetMapping("/invoices/business/{businessId}")
    public ResponseEntity<List<InvoiceDTO>> getByBusinessId(@PathVariable Long businessId) {
        return ResponseEntity.ok(invoiceService.getByBusinessId(businessId));
    }

    // GET BY STATUS - /api/invoice/invoices/status/PAID
    @GetMapping("/invoices/status/{status}")
    public ResponseEntity<List<InvoiceDTO>> getByStatus(@PathVariable String status) {
        return ResponseEntity.ok(invoiceService.getByStatus(status));
    }

    // SEARCH BY CLIENT NAME - /api/invoice/invoices/search?clientName=ABC
    @GetMapping("/invoices/search")
    public ResponseEntity<List<InvoiceDTO>> searchByClientName(@RequestParam String clientName) {
        List<InvoiceDTO> invoices = invoiceService.searchByClientName(clientName);
        if (invoices.isEmpty()) return ResponseEntity.noContent().build();
        return ResponseEntity.ok(invoices);
    }

    // POST - Créer une facture
    @PostMapping(value = "/invoices", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<InvoiceDTO> createInvoice(@RequestBody InvoiceDTO invoiceDTO) {
        return new ResponseEntity<>(invoiceService.createInvoice(invoiceDTO), HttpStatus.CREATED);
    }

    // PUT - Modifier une facture
    @PutMapping("/invoices/{id}")
    public ResponseEntity<InvoiceDTO> updateInvoice(@PathVariable Long id,
                                                    @RequestBody InvoiceDTO invoiceDTO) {
        return ResponseEntity.ok(invoiceService.updateInvoice(id, invoiceDTO));
    }

    // DELETE - Supprimer une facture
    @DeleteMapping("/invoices/{id}")
    public ResponseEntity<String> deleteInvoice(@PathVariable Long id) {
        return ResponseEntity.ok(invoiceService.deleteInvoice(id));
    }
}
