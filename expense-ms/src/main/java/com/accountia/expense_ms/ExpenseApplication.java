package com.accountia.expense_ms;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@SpringBootApplication
@EnableDiscoveryClient
@EnableFeignClients(basePackages = "com.accountia.expense.client")
@ComponentScan(basePackages = "com.accountia.expense")
@EntityScan(basePackages = "com.accountia.expense.model")
@EnableJpaRepositories(basePackages = "com.accountia.expense.repository")
public class ExpenseApplication {
    public static void main(String[] args) {
        SpringApplication.run(ExpenseApplication.class, args);
    }
}
