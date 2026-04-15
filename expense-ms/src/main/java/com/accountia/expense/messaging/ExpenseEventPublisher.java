package com.accountia.expense.messaging;

import com.accountia.expense.config.RabbitMQConfig;
import com.accountia.expense.model.Expense;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

@Component
@RequiredArgsConstructor
@Slf4j
public class ExpenseEventPublisher {

    private final RabbitTemplate rabbitTemplate;

    public void publishExpenseCreated(Expense expense) {
        publishEvent(expense, "CREATED", RabbitMQConfig.EXPENSE_CREATED_ROUTING_KEY);
    }

    public void publishExpenseUpdated(Expense expense) {
        publishEvent(expense, "UPDATED", RabbitMQConfig.EXPENSE_UPDATED_ROUTING_KEY);
    }

    public void publishExpenseDeleted(String expenseId, String businessId) {
        Map<String, Object> message = new HashMap<>();
        message.put("expenseId", expenseId);
        message.put("businessId", businessId);
        message.put("eventType", "DELETED");
        message.put("timestamp", System.currentTimeMillis());

        log.info("Publishing expense deleted event for id: {}", expenseId);
        rabbitTemplate.convertAndSend(RabbitMQConfig.ACCOUNTIA_EXCHANGE, 
                RabbitMQConfig.EXPENSE_DELETED_ROUTING_KEY, message);
    }

    private void publishEvent(Expense expense, String eventType, String routingKey) {
        Map<String, Object> message = new HashMap<>();
        message.put("expenseId", expense.getId());
        message.put("businessId", expense.getBusinessId());
        message.put("amount", expense.getAmount());
        message.put("category", expense.getCategory());
        message.put("date", expense.getDate());
        message.put("eventType", eventType);
        message.put("timestamp", System.currentTimeMillis());

        log.info("Publishing expense {} event for id: {}", eventType, expense.getId());
        rabbitTemplate.convertAndSend(RabbitMQConfig.ACCOUNTIA_EXCHANGE, routingKey, message);
    }
}
