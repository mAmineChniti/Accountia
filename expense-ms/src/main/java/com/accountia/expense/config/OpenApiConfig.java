package com.accountia.expense.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI expenseOpenApi() {
        return new OpenAPI()
            .info(new Info()
                .title("Accountia Expense API")
                .version("1.0.0")
                .description("Expense management microservice for Accountia"));
    }
}
