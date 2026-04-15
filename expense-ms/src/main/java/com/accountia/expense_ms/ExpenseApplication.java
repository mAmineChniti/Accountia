package com.accountia.expense_ms;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@EnableDiscoveryClient
@EnableFeignClients(basePackages = "com.accountia.expense.feign")
@EnableJpaRepositories(basePackages = "com.accountia.expense.repository")
@EntityScan(basePackages = "com.accountia.expense.entity")
@SpringBootApplication(scanBasePackages = {"com.accountia.expense_ms", "com.accountia.expense"})
public class ExpenseApplication {
    public static void main(String[] args) {
        SpringApplication.run(ExpenseApplication.class, args);
    }
}
