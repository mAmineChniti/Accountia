package com.accountia.business_ms;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@SpringBootApplication(scanBasePackages = {"com.accountia.business_ms", "com.accountia.business"})
@EnableDiscoveryClient
@EnableJpaRepositories(basePackages = "com.accountia.business.repository")
@EntityScan(basePackages = "com.accountia.business.entity")
public class BusinessApplication {
    public static void main(String[] args) {
        SpringApplication.run(BusinessApplication.class, args);
    }
}
