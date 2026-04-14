package com.accountia.invoice.messaging;

import com.accountia.invoice.config.RabbitMQConfig;
import com.accountia.invoice.dto.InvoiceDTO;
import com.accountia.invoice.dto.InvoiceEventEnvelope;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.stereotype.Service;

/**
 * Message producer for publishing invoice events to RabbitMQ.
 *
 * <p>This bean is only created when a {@link RabbitTemplate} is available in the
 * Spring context (i.e., when RabbitMQ auto-configuration is NOT excluded).
 *
 * <p>In Sprint 1, RabbitMQ is excluded via
 * {@code spring.autoconfigure.exclude: RabbitAutoConfiguration}, so this bean
 * is inactive. It becomes fully functional in Sprint 2 when RabbitMQ is added.
 *
 * <p>Events published:
 * <ul>
 *   <li>INVOICE_CREATED — fired after a new invoice is created</li>
 *   <li>INVOICE_UPDATED — fired after an invoice is modified</li>
 *   <li>INVOICE_DELETED — fired after an invoice is soft-deleted</li>
 * </ul>
 */
@Service
@ConditionalOnBean(RabbitTemplate.class)
public class InvoiceMessageProducer {

    private static final Logger log = LoggerFactory.getLogger(InvoiceMessageProducer.class);

    private final RabbitTemplate rabbitTemplate;

    public InvoiceMessageProducer(RabbitTemplate rabbitTemplate) {
        this.rabbitTemplate = rabbitTemplate;
    }

    /**
     * Publishes an INVOICE_CREATED event to RabbitMQ.
     *
     * @param invoice the invoice DTO to include in the event payload
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
     * Publishes an INVOICE_UPDATED event to RabbitMQ.
     *
     * @param invoice the updated invoice DTO
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
     * Publishes an INVOICE_DELETED event to RabbitMQ.
     *
     * @param invoiceId the ID of the deleted invoice (as a Long, from the legacy DTO)
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
