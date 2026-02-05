package com.accountia.business.messaging;

import com.accountia.business.config.RabbitMQConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

import java.util.Map;

/**
 * RabbitMQ message consumer for Business Service.
 * Listens to invoice events and updates business statistics.
 */
@Component
public class InvoiceEventConsumer {

    private static final Logger log = LoggerFactory.getLogger(InvoiceEventConsumer.class);

    /**
     * Listens for invoice-related events and processes them.
     * Updates business statistics based on invoice changes.
     * 
     * @param message The invoice event message containing invoice details
     */
    @RabbitListener(queues = RabbitMQConfig.BUSINESS_INVOICE_QUEUE)
    public void handleInvoiceEvent(Map<String, Object> message) {
        try {
            // Validate message is not null
            if (message == null) {
                log.error("Received null message");
                throw new IllegalArgumentException("Received null message");
            }
            
            // Safely extract eventType with type checking
            Object eventTypeObj = message.get("eventType");
            if (eventTypeObj != null && !(eventTypeObj instanceof String)) {
                log.error("Invalid eventType type in message: expected String, got {}. Raw message: {}", 
                    eventTypeObj.getClass().getName(), message);
                throw new IllegalArgumentException("eventType must be a String");
            }
            String eventType = (String) eventTypeObj;
            
            // Safely extract invoiceId with type checking
            Object invoiceIdObj = message.get("invoiceId");
            if (invoiceIdObj != null && !(invoiceIdObj instanceof String)) {
                log.error("Invalid invoiceId type in message: expected String, got {}. Raw message: {}", 
                    invoiceIdObj.getClass().getName(), message);
                throw new IllegalArgumentException("invoiceId must be a String");
            }
            String invoiceId = (String) invoiceIdObj;
            
            log.info("Received invoice event: eventType={}, invoiceId={}", eventType, invoiceId);
            log.debug("Full invoice event payload: {}", message);
            
            // Validate required fields - throw exception to trigger DLQ for invalid messages
            if (eventType == null || eventType.isBlank()) {
                throw new IllegalArgumentException(
                    String.format("Missing or empty eventType in message. invoiceId=%s", invoiceId));
            }
            if (invoiceId == null || invoiceId.isBlank()) {
                throw new IllegalArgumentException(
                    String.format("Missing or empty invoiceId in message. eventType=%s", eventType));
            }
            
            switch (eventType) {
                case "CREATED":
                    handleInvoiceCreated(invoiceId, message);
                    break;
                case "UPDATED":
                    handleInvoiceUpdated(invoiceId, message);
                    break;
                case "DELETED":
                    handleInvoiceDeleted(invoiceId, message);
                    break;
                default:
                    throw new IllegalArgumentException(
                        String.format("Unknown eventType '%s' for invoiceId=%s. Expected: CREATED, UPDATED, or DELETED", 
                            eventType, invoiceId));
            }
        } catch (Exception e) {
            log.error("Error processing invoice event: {}", e.getMessage(), e);
            throw e; // Rethrow to trigger DLQ if configured
        }
    }

    private void handleInvoiceCreated(String invoiceId, Map<String, Object> message) {
        log.info("Processing invoice created event for invoice: {}", invoiceId);
        // Update business revenue statistics
        // Update client statistics
        // Trigger any business rules
    }

    private void handleInvoiceUpdated(String invoiceId, Map<String, Object> message) {
        log.info("Processing invoice updated event for invoice: {}", invoiceId);
        // Recalculate affected statistics
        // Update any cached business data
    }

    private void handleInvoiceDeleted(String invoiceId, Map<String, Object> message) {
        log.info("Processing invoice deleted event for invoice: {}", invoiceId);
        // Update revenue calculations
        // Clean up related business data
    }
}
