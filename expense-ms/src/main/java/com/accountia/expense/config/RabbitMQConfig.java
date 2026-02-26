package com.accountia.expense.config;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.QueueBuilder;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.rabbit.config.SimpleRabbitListenerContainerFactory;
import org.springframework.amqp.rabbit.retry.RejectAndDontRequeueRecoverer;
import org.springframework.amqp.rabbit.config.RetryInterceptorBuilder;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMQConfig {

    public static final String EVENTS_EXCHANGE = "accountia.events";
    public static final String DLX_EXCHANGE = "accountia.events.dlx";

    public static final String BUSINESS_DELETED_QUEUE = "expense-ms.business.deleted";
    public static final String BUSINESS_DELETED_DLQ = "expense-ms.business.deleted.dlq";
    public static final String BUSINESS_DELETED_ROUTING_KEY = "business.deleted";
    public static final String BUSINESS_DELETED_DLQ_ROUTING_KEY = "business.deleted.dlq";

    @Bean
    public TopicExchange eventsExchange() {
        return new TopicExchange(EVENTS_EXCHANGE, true, false);
    }

    @Bean
    public TopicExchange deadLetterExchange() {
        return new TopicExchange(DLX_EXCHANGE, true, false);
    }

    @Bean
    public Queue businessDeletedQueue() {
        return QueueBuilder.durable(BUSINESS_DELETED_QUEUE)
            .withArgument("x-dead-letter-exchange", DLX_EXCHANGE)
            .withArgument("x-dead-letter-routing-key", BUSINESS_DELETED_DLQ_ROUTING_KEY)
            .build();
    }

    @Bean
    public Queue businessDeletedDlq() {
        return QueueBuilder.durable(BUSINESS_DELETED_DLQ).build();
    }

    @Bean
    public Binding businessDeletedBinding(Queue businessDeletedQueue, TopicExchange eventsExchange) {
        return BindingBuilder.bind(businessDeletedQueue).to(eventsExchange).with(BUSINESS_DELETED_ROUTING_KEY);
    }

    @Bean
    public Binding businessDeletedDlqBinding(Queue businessDeletedDlq, TopicExchange deadLetterExchange) {
        return BindingBuilder.bind(businessDeletedDlq).to(deadLetterExchange).with(BUSINESS_DELETED_DLQ_ROUTING_KEY);
    }

    @Bean
    public Jackson2JsonMessageConverter jackson2JsonMessageConverter() {
        return new Jackson2JsonMessageConverter();
    }

    @Bean
    public RabbitTemplate rabbitTemplate(ConnectionFactory connectionFactory, Jackson2JsonMessageConverter converter) {
        RabbitTemplate template = new RabbitTemplate(connectionFactory);
        template.setMessageConverter(converter);
        return template;
    }

    @Bean
    public SimpleRabbitListenerContainerFactory rabbitListenerContainerFactory(
        ConnectionFactory connectionFactory,
        Jackson2JsonMessageConverter converter
    ) {
        SimpleRabbitListenerContainerFactory factory = new SimpleRabbitListenerContainerFactory();
        factory.setConnectionFactory(connectionFactory);
        factory.setMessageConverter(converter);
        factory.setDefaultRequeueRejected(false);
        factory.setAdviceChain(RetryInterceptorBuilder.stateless()
            .maxAttempts(3)
            .backOffOptions(1000, 2.0, 10000)
            .recoverer(new RejectAndDontRequeueRecoverer())
            .build());
        return factory;
    }
}
