package com.accountia.expense.messaging;

import com.accountia.expense.config.RabbitMQConfig;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;

@Component
public class EventPublisher {

    private final RabbitTemplate rabbitTemplate;

    public EventPublisher(RabbitTemplate rabbitTemplate) {
        this.rabbitTemplate = rabbitTemplate;
    }

    public void publishExpenseSubmitted(EventPayloads.ExpenseSubmittedEvent event) {
        rabbitTemplate.convertAndSend(RabbitMQConfig.EVENTS_EXCHANGE, "expense.submitted", event);
    }

    public void publishExpenseApproved(EventPayloads.ExpenseApprovedEvent event) {
        rabbitTemplate.convertAndSend(RabbitMQConfig.EVENTS_EXCHANGE, "expense.approved", event);
    }

    public void publishExpenseRejected(EventPayloads.ExpenseRejectedEvent event) {
        rabbitTemplate.convertAndSend(RabbitMQConfig.EVENTS_EXCHANGE, "expense.rejected", event);
    }

    public void publishExpenseDeleted(EventPayloads.ExpenseDeletedEvent event) {
        rabbitTemplate.convertAndSend(RabbitMQConfig.EVENTS_EXCHANGE, "expense.deleted", event);
    }

    public void publishBudgetExceeded(EventPayloads.BudgetExceededEvent event) {
        rabbitTemplate.convertAndSend(RabbitMQConfig.EVENTS_EXCHANGE, "expense.budget.exceeded", event);
    }
}
