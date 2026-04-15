package com.accountia.business.messaging;

import com.accountia.business.config.RabbitMQConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

import java.util.Map;

/**
 * RabbitMQ message consumer for Expense events in Business Service.
 * Listens to expense events and updates business statistics.
 */
@Component
public class ExpenseEventConsumer {

    private static final Logger log = LoggerFactory.getLogger(ExpenseEventConsumer.class);

    /**
     * Listens for expense-related events and processes them.
     * Updates business statistics based on expense changes.
     * 
     * @param message The expense event message containing expense details
     */
    @RabbitListener(queues = RabbitMQConfig.BUSINESS_EXPENSE_QUEUE)
    public void handleExpenseEvent(Map<String, Object> message) {
        try {
            if (message == null) {
                log.error("Received null expense message");
                return;
            }

            String eventType = (String) message.get("eventType");
            String expenseId = (String) message.get("expenseId");
            String businessId = (String) message.get("businessId");

            log.info("Received expense event: eventType={}, expenseId={}, businessId={}", 
                    eventType, expenseId, businessId);

            if (eventType == null) {
                log.error("Missing eventType in expense message");
                return;
            }

            switch (eventType) {
                case "CREATED":
                    log.info("Processing expense creation for business: {}", businessId);
                    // TODO: Update business statistics (e.g. increase total expenses)
                    break;
                case "UPDATED":
                    log.info("Processing expense update for business: {}", businessId);
                    // TODO: Recalculate business statistics
                    break;
                case "DELETED":
                    log.info("Processing expense deletion for business: {}", businessId);
                    // TODO: Update business statistics (e.g. decrease total expenses)
                    break;
                default:
                    log.warn("Unknown eventType received: {}", eventType);
            }
        } catch (Exception e) {
            log.error("Error processing expense event: {}", e.getMessage(), e);
        }
    }
}
