package com.accountia.expense.config;

import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMQConfig {

    public static final String ACCOUNTIA_EXCHANGE = "accountia.exchange";
    public static final String EXPENSE_QUEUE = "expense.queue";
    public static final String EXPENSE_DLQ = "expense.dlq";

    public static final String EXPENSE_CREATED_ROUTING_KEY = "expense.created";
    public static final String EXPENSE_UPDATED_ROUTING_KEY = "expense.updated";
    public static final String EXPENSE_DELETED_ROUTING_KEY = "expense.deleted";

    @Bean
    public TopicExchange accountiaExchange() {
        return ExchangeBuilder
                .topicExchange(ACCOUNTIA_EXCHANGE)
                .durable(true)
                .build();
    }

    @Bean
    public Queue expenseQueue() {
        return QueueBuilder
                .durable(EXPENSE_QUEUE)
                .withArgument("x-dead-letter-exchange", "")
                .withArgument("x-dead-letter-routing-key", EXPENSE_DLQ)
                .build();
    }

    @Bean
    public Queue expenseDeadLetterQueue() {
        return QueueBuilder
                .durable(EXPENSE_DLQ)
                .build();
    }

    @Bean
    public Binding expenseCreatedBinding(Queue expenseQueue, TopicExchange accountiaExchange) {
        return BindingBuilder
                .bind(expenseQueue)
                .to(accountiaExchange)
                .with(EXPENSE_CREATED_ROUTING_KEY);
    }

    @Bean
    public Binding expenseUpdatedBinding(Queue expenseQueue, TopicExchange accountiaExchange) {
        return BindingBuilder
                .bind(expenseQueue)
                .to(accountiaExchange)
                .with(EXPENSE_UPDATED_ROUTING_KEY);
    }

    @Bean
    public Binding expenseDeletedBinding(Queue expenseQueue, TopicExchange accountiaExchange) {
        return BindingBuilder
                .bind(expenseQueue)
                .to(accountiaExchange)
                .with(EXPENSE_DELETED_ROUTING_KEY);
    }

    @Bean
    public MessageConverter jsonMessageConverter() {
        return new Jackson2JsonMessageConverter();
    }

    @Bean
    public RabbitTemplate rabbitTemplate(ConnectionFactory connectionFactory) {
        RabbitTemplate rabbitTemplate = new RabbitTemplate(connectionFactory);
        rabbitTemplate.setMessageConverter(jsonMessageConverter());
        return rabbitTemplate;
    }
}
