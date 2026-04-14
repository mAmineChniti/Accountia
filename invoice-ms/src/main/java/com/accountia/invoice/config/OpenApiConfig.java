package com.accountia.invoice.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

/**
 * Swagger / OpenAPI 3 configuration.
 *
 * <p>The generated documentation is available at:
 * <ul>
 *   <li>Swagger UI: {@code http://localhost:8080/swagger-ui.html}</li>
 *   <li>Raw JSON: {@code http://localhost:8080/v3/api-docs}</li>
 * </ul>
 *
 * <p>The "BearerAuth" security scheme is pre-configured so testers can
 * click "Authorize" in Swagger UI, paste a JWT, and call all endpoints
 * without manually adding the Authorization header each time.
 */
@Configuration
public class OpenApiConfig {

    private static final String SECURITY_SCHEME_NAME = "BearerAuth";

    @Bean
    public OpenAPI invoiceServiceOpenAPI() {
        return new OpenAPI()
            .info(new Info()
                .title("Invoice Microservice API")
                .version("1.0.0")
                .description("""
                    REST API for the Accountia Invoice Microservice.

                    **Features:**
                    - Full invoice lifecycle (DRAFT → ISSUED → VIEWED → PAID → ARCHIVED)
                    - Received invoice inbox for platform businesses and individuals
                    - Recurring invoice schedules
                    - Threaded comments on invoices
                    - CSV/Excel bulk import
                    - AI-powered payment risk prediction
                    - Anomaly detection before invoice finalization

                    **Authentication:** Bearer JWT token in Authorization header.
                    """)
                .contact(new Contact()
                    .name("Accountia Platform")
                    .email("dev@accountia.io"))
                .license(new License()
                    .name("Private — Accountia SaaS"))
            )
            .servers(List.of(
                new Server().url("http://localhost:8080").description("Local development")
            ))
            // Tell Swagger UI that all endpoints require Bearer JWT by default
            .addSecurityItem(new SecurityRequirement().addList(SECURITY_SCHEME_NAME))
            .components(new Components()
                .addSecuritySchemes(SECURITY_SCHEME_NAME, new SecurityScheme()
                    .name(SECURITY_SCHEME_NAME)
                    .type(SecurityScheme.Type.HTTP)
                    .scheme("bearer")
                    .bearerFormat("JWT")
                    .description("Paste the JWT token obtained from the Auth service login endpoint")
                )
            );
    }
}
