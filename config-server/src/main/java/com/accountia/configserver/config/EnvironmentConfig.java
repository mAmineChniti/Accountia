package com.accountia.configserver.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class EnvironmentConfig {

    @Bean
    @ConfigurationProperties(prefix = "secure")
    public SecureProperties secureProperties() {
        return new SecureProperties();
    }

    public static class SecureProperties {
        private String jwtSecret;
        private String datasourcePassword;
        private String rabbitmqPassword;
        private String redisPassword;

        public String getJwtSecret() {
            return jwtSecret;
        }

        public void setJwtSecret(String jwtSecret) {
            this.jwtSecret = jwtSecret;
        }

        public String getDatasourcePassword() {
            return datasourcePassword;
        }

        public void setDatasourcePassword(String datasourcePassword) {
            this.datasourcePassword = datasourcePassword;
        }

        public String getRabbitmqPassword() {
            return rabbitmqPassword;
        }

        public void setRabbitmqPassword(String rabbitmqPassword) {
            this.rabbitmqPassword = rabbitmqPassword;
        }

        public String getRedisPassword() {
            return redisPassword;
        }

        public void setRedisPassword(String redisPassword) {
            this.redisPassword = redisPassword;
        }
    }
}
