package com.accountia.auth_ms;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@SpringBootApplication(scanBasePackages = {"com.accountia.auth_ms", "com.accountia.auth"})
@EnableDiscoveryClient
@EnableJpaRepositories(basePackages = "com.accountia.auth.repository")
@EntityScan(basePackages = {"com.accountia.auth.model"})
public class AuthApplication {

    public static void main(String[] args) {
        SpringApplication.run(AuthApplication.class, args);
    }

}
