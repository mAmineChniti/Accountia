package com.accountia.business.config;

import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * RabbitMQ configuration for Business Service.
 * Defines exchanges, queues, and bindings for business-related events.
 */
@Configuration
public class RabbitMQConfig {

    // Exchange names
    public static final String ACCOUNTIA_EXCHANGE = "accountia.exchange";
    
    // Queue names
    public static final String BUSINESS_QUEUE = "business.queue";
    public static final String BUSINESS_INVOICE_QUEUE = "business.invoice.queue";
    public static final String BUSINESS_DLQ = "business.dlq";
    
    // Routing keys
    public static final String BUSINESS_ROUTING_KEY = "business.#";
    public static final String INVOICE_CREATED_ROUTING_KEY = "invoice.created";
    public static final String INVOICE_UPDATED_ROUTING_KEY = "invoice.updated";

    /**
     * Topic exchange for all Accountia events.
     * Using topic exchange allows flexible routing based on patterns.
     */
    @Bean
    public TopicExchange accountiaExchange() {
        return ExchangeBuilder
            .topicExchange(ACCOUNTIA_EXCHANGE)
            .durable(true)
            .build();
    }

    /**
     * Main business events queue with dead letter routing.
     */
    @Bean
    public Queue businessQueue() {
        return QueueBuilder
            .durable(BUSINESS_QUEUE)
            .withArgument("x-dead-letter-exchange", "")
            .withArgument("x-dead-letter-routing-key", BUSINESS_DLQ)
            .build();
    }

    /**
     * Queue for receiving invoice events.
     */
    @Bean
    public Queue businessInvoiceQueue() {
        return QueueBuilder
            .durable(BUSINESS_INVOICE_QUEUE)
            .withArgument("x-dead-letter-exchange", "")
            .withArgument("x-dead-letter-routing-key", BUSINESS_DLQ)
            .build();
    }

    /**
     * Dead letter queue for failed messages.
     */
    @Bean
    public Queue businessDeadLetterQueue() {
        return QueueBuilder
            .durable(BUSINESS_DLQ)
            .build();
    }

    /**
     * Binding for general business events.
     */
    @Bean
    public Binding businessBinding(@Qualifier("businessQueue") Queue businessQueue,
                                    @Qualifier("accountiaExchange") TopicExchange accountiaExchange) {
        return BindingBuilder
            .bind(businessQueue)
            .to(accountiaExchange)
            .with(BUSINESS_ROUTING_KEY);
    }

    /**
     * Binding for invoice created events.
     */
    @Bean
    public Binding invoiceCreatedBinding(@Qualifier("businessInvoiceQueue") Queue businessInvoiceQueue,
                                          @Qualifier("accountiaExchange") TopicExchange accountiaExchange) {
        return BindingBuilder
            .bind(businessInvoiceQueue)
            .to(accountiaExchange)
            .with(INVOICE_CREATED_ROUTING_KEY);
    }

    /**
     * Binding for invoice updated events.
     */
    @Bean
    public Binding invoiceUpdatedBinding(@Qualifier("businessInvoiceQueue") Queue businessInvoiceQueue,
                                          @Qualifier("accountiaExchange") TopicExchange accountiaExchange) {
        return BindingBuilder
            .bind(businessInvoiceQueue)
            .to(accountiaExchange)
            .with(INVOICE_UPDATED_ROUTING_KEY);
    }

    /**
     * JSON message converter for RabbitMQ.
     */
    @Bean
    public MessageConverter jsonMessageConverter() {
        return new Jackson2JsonMessageConverter();
    }

    /**
     * RabbitTemplate with JSON message converter.
     */
    @Bean
    public RabbitTemplate rabbitTemplate(ConnectionFactory connectionFactory) {
        RabbitTemplate rabbitTemplate = new RabbitTemplate(connectionFactory);
        rabbitTemplate.setMessageConverter(jsonMessageConverter());
        return rabbitTemplate;
    }
}
