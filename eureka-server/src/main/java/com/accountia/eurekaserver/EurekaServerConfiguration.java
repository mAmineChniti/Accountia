package com.accountia.eurekaserver;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Minimal Eureka Server Configuration
 * Following Spring documentation best practices
 */
@Configuration
public class EurekaServerConfiguration {

    /**
     * Optional: Custom instance configuration if needed
     * Most applications don't need this - Spring handles it automatically
     */
    // @Bean
    // public ApplicationInfoManager applicationInfoManager() {
    //     // Custom configuration if needed
    // }
}
