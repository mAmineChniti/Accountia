package com.accountia.invoice;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * Entry point for the Invoice microservice.
 *
 * <p>Sprint 1 responsibilities:
 * <ul>
 *   <li>Full invoice lifecycle (DRAFT → ISSUED → VIEWED → PAID → ARCHIVED)</li>
 *   <li>Received invoices inbox for platform users</li>
 *   <li>Recurring invoice schedules</li>
 *   <li>Threaded comments on invoices</li>
 *   <li>CSV/Excel import and PDF export</li>
 *   <li>AI payment prediction and anomaly detection</li>
 * </ul>
 *
 * <p>Sprint 2 additions (not yet active):
 * {@code @EnableDiscoveryClient} registers with Eureka when enabled.
 * RabbitMQ messaging is wired but excluded from auto-configuration until Sprint 2.
 *
 * <p>Note: {@code @EnableJpaAuditing} lives in {@link com.accountia.invoice.config.JpaConfig}
 * (not here) so that the {@code auditorAwareRef} is correctly wired to {@link
 * com.accountia.invoice.security.AuditAwareImpl}. Declaring it in two places causes a
 * duplicate {@code jpaAuditingHandler} bean conflict.
 */
@SpringBootApplication
@EnableDiscoveryClient          // Eureka registration (Sprint 2: set eureka.client.enabled=true)
@EnableScheduling               // Activates @Scheduled overdue-detection job
public class InvoiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(InvoiceApplication.class, args);
    }
}
