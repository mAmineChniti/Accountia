package com.accountia.invoice.config;

import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * RabbitMQ configuration for Invoice microservice.
 * Defines exchanges, queues, and bindings for async messaging.
 */
@Configuration
public class RabbitMQConfig {

    // Exchange names
    public static final String ACCOUNTIA_EXCHANGE = "accountia.exchange";
    public static final String ACCOUNTIA_DLX_EXCHANGE = "accountia.dlx.exchange";

    // Queue names
    public static final String INVOICE_QUEUE = "invoice.queue";
    public static final String INVOICE_CREATED_QUEUE = "invoice.created.queue";
    public static final String INVOICE_UPDATED_QUEUE = "invoice.updated.queue";
    public static final String INVOICE_DLQ = "invoice.dlq";

    // Routing keys
    public static final String INVOICE_ROUTING_KEY = "invoice.#";
    public static final String INVOICE_CREATED_ROUTING_KEY = "invoice.created";
    public static final String INVOICE_UPDATED_ROUTING_KEY = "invoice.updated";
    public static final String INVOICE_DELETED_ROUTING_KEY = "invoice.deleted";

    // Main Exchange
    @Bean
    public TopicExchange accountiaExchange() {
        return new TopicExchange(ACCOUNTIA_EXCHANGE, true, false);
    }

    // Dead Letter Exchange
    @Bean
    public DirectExchange deadLetterExchange() {
        return new DirectExchange(ACCOUNTIA_DLX_EXCHANGE, true, false);
    }

    // Invoice Queue with DLX
    @Bean
    public Queue invoiceQueue() {
        return QueueBuilder.durable(INVOICE_QUEUE)
                .withArgument("x-dead-letter-exchange", ACCOUNTIA_DLX_EXCHANGE)
                .withArgument("x-dead-letter-routing-key", INVOICE_DLQ)
                .build();
    }

    // Invoice Created Queue
    @Bean
    public Queue invoiceCreatedQueue() {
        return QueueBuilder.durable(INVOICE_CREATED_QUEUE)
                .withArgument("x-dead-letter-exchange", ACCOUNTIA_DLX_EXCHANGE)
                .withArgument("x-dead-letter-routing-key", INVOICE_DLQ)
                .build();
    }

    // Invoice Updated Queue
    @Bean
    public Queue invoiceUpdatedQueue() {
        return QueueBuilder.durable(INVOICE_UPDATED_QUEUE)
                .withArgument("x-dead-letter-exchange", ACCOUNTIA_DLX_EXCHANGE)
                .withArgument("x-dead-letter-routing-key", INVOICE_DLQ)
                .build();
    }

    // Dead Letter Queue
    @Bean
    public Queue deadLetterQueue() {
        return QueueBuilder.durable(INVOICE_DLQ).build();
    }

    // Bindings
    @Bean
    public Binding invoiceBinding(@Qualifier("invoiceQueue") Queue invoiceQueue,
                                   @Qualifier("accountiaExchange") TopicExchange accountiaExchange) {
        return BindingBuilder.bind(invoiceQueue).to(accountiaExchange).with(INVOICE_ROUTING_KEY);
    }

    @Bean
    public Binding invoiceCreatedBinding(@Qualifier("invoiceCreatedQueue") Queue invoiceCreatedQueue,
                                          @Qualifier("accountiaExchange") TopicExchange accountiaExchange) {
        return BindingBuilder.bind(invoiceCreatedQueue).to(accountiaExchange).with(INVOICE_CREATED_ROUTING_KEY);
    }

    @Bean
    public Binding invoiceUpdatedBinding(@Qualifier("invoiceUpdatedQueue") Queue invoiceUpdatedQueue,
                                          @Qualifier("accountiaExchange") TopicExchange accountiaExchange) {
        return BindingBuilder.bind(invoiceUpdatedQueue).to(accountiaExchange).with(INVOICE_UPDATED_ROUTING_KEY);
    }

    @Bean
    public Binding dlqBinding(@Qualifier("deadLetterQueue") Queue deadLetterQueue,
                               @Qualifier("deadLetterExchange") DirectExchange deadLetterExchange) {
        return BindingBuilder.bind(deadLetterQueue).to(deadLetterExchange).with(INVOICE_DLQ);
    }

    // JSON Message Converter
    @Bean
    public MessageConverter jsonMessageConverter() {
        return new Jackson2JsonMessageConverter();
    }

    // RabbitTemplate with JSON converter
    @Bean
    public RabbitTemplate rabbitTemplate(ConnectionFactory connectionFactory) {
        RabbitTemplate rabbitTemplate = new RabbitTemplate(connectionFactory);
        rabbitTemplate.setMessageConverter(jsonMessageConverter());
        return rabbitTemplate;
    }
}
