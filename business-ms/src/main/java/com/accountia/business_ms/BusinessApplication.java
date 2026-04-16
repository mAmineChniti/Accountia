package com.accountia.business_ms;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.boot.autoconfigure.domain.EntityScan;

/**
 * Point d'entrée du microservice Business.
 *
 * @EnableFeignClients : active OpenFeign pour les appels vers client-ms
 * @EnableDiscoveryClient : s'enregistre sur Eureka
 */
@SpringBootApplication(scanBasePackages = {"com.accountia.business_ms", "com.accountia.business"})
@EnableDiscoveryClient
@EnableFeignClients(basePackages = "com.accountia.business.feign")
@EnableJpaRepositories(basePackages = "com.accountia.business.repository")
@EntityScan(basePackages = "com.accountia.business.entity")
public class BusinessApplication {
    public static void main(String[] args) {
        SpringApplication.run(BusinessApplication.class, args);
    }
}