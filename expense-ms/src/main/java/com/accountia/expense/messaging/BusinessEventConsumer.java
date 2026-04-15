package com.accountia.expense.messaging;

import com.accountia.expense.config.RabbitMQConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
public class BusinessEventConsumer {

    private static final Logger log = LoggerFactory.getLogger(BusinessEventConsumer.class);

    @RabbitListener(queues = RabbitMQConfig.BUSINESS_QUEUE)
    public void handleBusinessEvent(Map<String, Object> message) {
        if (message == null) {
            log.error("Received null business event message");
            throw new IllegalArgumentException("Received null business event message");
        }

        String eventType = (String) message.get("eventType");
        Object businessId = message.get("businessId");

        log.info("Received business event: eventType={}, businessId={}", eventType, businessId);

        if (eventType == null || eventType.isBlank()) {
            throw new IllegalArgumentException("Missing or empty eventType in business event message");
        }

        switch (eventType) {
            case "CREATED" -> handleBusinessCreated(message);
            case "UPDATED" -> handleBusinessUpdated(message);
            case "DELETED" -> handleBusinessDeleted(message);
            default -> throw new IllegalArgumentException("Unknown business eventType: " + eventType);
        }
    }

    private void handleBusinessCreated(Map<String, Object> message) {
        log.info("Business created event received in expense-ms: {}", message);
    }

    private void handleBusinessUpdated(Map<String, Object> message) {
        log.info("Business updated event received in expense-ms: {}", message);
    }

    private void handleBusinessDeleted(Map<String, Object> message) {
        log.warn("Business deleted event received in expense-ms: {}", message);
    }
}