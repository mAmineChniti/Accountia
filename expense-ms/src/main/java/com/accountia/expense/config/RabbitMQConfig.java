package com.accountia.expense.config;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.QueueBuilder;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.amqp.core.ExchangeBuilder;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMQConfig {

    public static final String ACCOUNTIA_EXCHANGE = "accountia.exchange";
    public static final String BUSINESS_QUEUE = "business.queue";
    public static final String BUSINESS_DLQ = "business.dlq";
    public static final String BUSINESS_CREATED_ROUTING_KEY = "business.created";
    public static final String BUSINESS_UPDATED_ROUTING_KEY = "business.updated";
    public static final String BUSINESS_DELETED_ROUTING_KEY = "business.deleted";

    @Bean
    public TopicExchange accountiaExchange() {
        return ExchangeBuilder.topicExchange(ACCOUNTIA_EXCHANGE).durable(true).build();
    }

    @Bean
    public Queue businessQueue() {
        return QueueBuilder.durable(BUSINESS_QUEUE)
                .withArgument("x-dead-letter-exchange", "")
                .withArgument("x-dead-letter-routing-key", BUSINESS_DLQ)
                .build();
    }

    @Bean
    public Queue businessDeadLetterQueue() {
        return QueueBuilder.durable(BUSINESS_DLQ).build();
    }

    @Bean
    public Binding businessCreatedBinding(@Qualifier("businessQueue") Queue businessQueue,
                                          @Qualifier("accountiaExchange") TopicExchange accountiaExchange) {
        return BindingBuilder.bind(businessQueue).to(accountiaExchange).with(BUSINESS_CREATED_ROUTING_KEY);
    }

    @Bean
    public Binding businessUpdatedBinding(@Qualifier("businessQueue") Queue businessQueue,
                                          @Qualifier("accountiaExchange") TopicExchange accountiaExchange) {
        return BindingBuilder.bind(businessQueue).to(accountiaExchange).with(BUSINESS_UPDATED_ROUTING_KEY);
    }

    @Bean
    public Binding businessDeletedBinding(@Qualifier("businessQueue") Queue businessQueue,
                                          @Qualifier("accountiaExchange") TopicExchange accountiaExchange) {
        return BindingBuilder.bind(businessQueue).to(accountiaExchange).with(BUSINESS_DELETED_ROUTING_KEY);
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