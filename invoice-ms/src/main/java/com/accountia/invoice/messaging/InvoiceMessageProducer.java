package com.accountia.invoice.messaging;

import com.accountia.invoice.config.RabbitMQConfig;
import com.accountia.invoice.dto.InvoiceDTO;
import com.accountia.invoice.dto.InvoiceEventEnvelope;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;

/**
 * Message producer for publishing invoice events to RabbitMQ.
 */
@Service
public class InvoiceMessageProducer {

    private static final Logger log = LoggerFactory.getLogger(InvoiceMessageProducer.class);

    private final RabbitTemplate rabbitTemplate;

    public InvoiceMessageProducer(RabbitTemplate rabbitTemplate) {
        this.rabbitTemplate = rabbitTemplate;
    }

    /**
     * Publish invoice created event.
     */
    public void publishInvoiceCreated(InvoiceDTO invoice) {
        log.info("Publishing invoice created event for invoice ID: {}", invoice.getId());
        InvoiceEventEnvelope envelope = InvoiceEventEnvelope.created(invoice);
        rabbitTemplate.convertAndSend(
                RabbitMQConfig.ACCOUNTIA_EXCHANGE,
                RabbitMQConfig.INVOICE_CREATED_ROUTING_KEY,
                envelope
        );
    }

    /**
     * Publish invoice updated event.
     */
    public void publishInvoiceUpdated(InvoiceDTO invoice) {
        log.info("Publishing invoice updated event for invoice ID: {}", invoice.getId());
        InvoiceEventEnvelope envelope = InvoiceEventEnvelope.updated(invoice);
        rabbitTemplate.convertAndSend(
                RabbitMQConfig.ACCOUNTIA_EXCHANGE,
                RabbitMQConfig.INVOICE_UPDATED_ROUTING_KEY,
                envelope
        );
    }

    /**
     * Publish invoice deleted event.
     */
    public void publishInvoiceDeleted(Long invoiceId) {
        log.info("Publishing invoice deleted event for invoice ID: {}", invoiceId);
        InvoiceEventEnvelope envelope = InvoiceEventEnvelope.deleted(invoiceId);
        rabbitTemplate.convertAndSend(
                RabbitMQConfig.ACCOUNTIA_EXCHANGE,
                RabbitMQConfig.INVOICE_DELETED_ROUTING_KEY,
                envelope
        );
    }
}
