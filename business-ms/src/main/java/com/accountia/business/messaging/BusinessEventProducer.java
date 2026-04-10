package com.accountia.business.messaging;

import com.accountia.business.config.RabbitMQConfig;
import com.accountia.business.entity.Business;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.Map;

@Service
public class BusinessEventProducer {

    private static final Logger log = LoggerFactory.getLogger(BusinessEventProducer.class);

    private final RabbitTemplate rabbitTemplate;

    public BusinessEventProducer(RabbitTemplate rabbitTemplate) {
        this.rabbitTemplate = rabbitTemplate;
    }

    public void publishBusinessCreated(Business business) {
        publish("CREATED", business);
    }

    public void publishBusinessUpdated(Business business) {
        publish("UPDATED", business);
    }

    public void publishBusinessDeleted(Long businessId) {
        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("eventType", "DELETED");
        payload.put("businessId", businessId);
        payload.put("timestamp", Instant.now().toString());

        log.info("Publishing business deleted event for business ID: {}", businessId);
        rabbitTemplate.convertAndSend(
                RabbitMQConfig.ACCOUNTIA_EXCHANGE,
                RabbitMQConfig.BUSINESS_DELETED_ROUTING_KEY,
                payload
        );
    }

    private void publish(String eventType, Business business) {
        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("eventType", eventType);
        payload.put("businessId", business.getId());
        payload.put("nom", business.getNom());
        payload.put("secteur", business.getSecteur());
        payload.put("ownerUserId", business.getOwnerUserId());
        payload.put("timestamp", Instant.now().toString());

        log.info("Publishing business {} event for business ID: {}", eventType.toLowerCase(), business.getId());
        rabbitTemplate.convertAndSend(
                RabbitMQConfig.ACCOUNTIA_EXCHANGE,
                switch (eventType) {
                    case "CREATED" -> RabbitMQConfig.BUSINESS_CREATED_ROUTING_KEY;
                    case "UPDATED" -> RabbitMQConfig.BUSINESS_UPDATED_ROUTING_KEY;
                    default -> throw new IllegalArgumentException("Unsupported business event type: " + eventType);
                },
                payload
        );
    }
}