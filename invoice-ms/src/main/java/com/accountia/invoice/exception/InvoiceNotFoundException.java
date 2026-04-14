package com.accountia.invoice.exception;

/** Thrown when an invoice ID cannot be found or doesn't belong to the requested business. */
public class InvoiceNotFoundException extends RuntimeException {
    public InvoiceNotFoundException(String invoiceId) {
        super("Invoice not found: " + invoiceId);
    }
}
