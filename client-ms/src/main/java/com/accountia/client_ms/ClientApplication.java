package com.accountia.client_ms;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@SpringBootApplication(scanBasePackages = {"com.accountia.client_ms", "com.accountia.client"})
@EnableDiscoveryClient
@EnableJpaRepositories(basePackages = "com.accountia.client.repository")
@EntityScan(basePackages = "com.accountia.client.entity")
public class ClientApplication {
    public static void main(String[] args) {
        SpringApplication.run(ClientApplication.class, args);
    }
}
