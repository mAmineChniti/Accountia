package com.accountia.expense.messaging;

import com.accountia.expense.config.RabbitMQConfig;
import com.accountia.expense.service.ExpenseService;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Component
public class EventConsumer {

    private final ExpenseService expenseService;

    public EventConsumer(ExpenseService expenseService) {
        this.expenseService = expenseService;
    }

    @RabbitListener(queues = RabbitMQConfig.BUSINESS_DELETED_QUEUE)
    public void handleBusinessDeleted(EventPayloads.BusinessDeletedEvent event) {
        expenseService.softDeleteByBusiness(event.businessId());
    }
}
