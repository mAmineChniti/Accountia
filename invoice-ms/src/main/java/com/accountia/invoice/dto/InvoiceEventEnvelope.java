package com.accountia.invoice.dto;

import java.io.Serializable;

/**
 * Event envelope for invoice messages sent to RabbitMQ.
 * Provides a consistent contract for invoice event consumers.
 */
public class InvoiceEventEnvelope implements Serializable {

    private static final long serialVersionUID = 1L;

    private String eventType;
    private String invoiceId;
    private InvoiceDTO invoice;

    public InvoiceEventEnvelope() {
    }

    public InvoiceEventEnvelope(String eventType, String invoiceId, InvoiceDTO invoice) {
        this.eventType = eventType;
        this.invoiceId = invoiceId;
        this.invoice = invoice;
    }

    public static InvoiceEventEnvelope created(InvoiceDTO invoice) {
        return new InvoiceEventEnvelope("CREATED", String.valueOf(invoice.getId()), invoice);
    }

    public static InvoiceEventEnvelope updated(InvoiceDTO invoice) {
        return new InvoiceEventEnvelope("UPDATED", String.valueOf(invoice.getId()), invoice);
    }

    public static InvoiceEventEnvelope deleted(Long invoiceId) {
        return new InvoiceEventEnvelope("DELETED", String.valueOf(invoiceId), null);
    }

    public String getEventType() {
        return eventType;
    }

    public void setEventType(String eventType) {
        this.eventType = eventType;
    }

    public String getInvoiceId() {
        return invoiceId;
    }

    public void setInvoiceId(String invoiceId) {
        this.invoiceId = invoiceId;
    }

    public InvoiceDTO getInvoice() {
        return invoice;
    }

    public void setInvoice(InvoiceDTO invoice) {
        this.invoice = invoice;
    }

    @Override
    public String toString() {
        return "InvoiceEventEnvelope{" +
                "eventType='" + eventType + '\'' +
                ", invoiceId='" + invoiceId + '\'' +
                ", invoice=" + invoice +
                '}';
    }
}
