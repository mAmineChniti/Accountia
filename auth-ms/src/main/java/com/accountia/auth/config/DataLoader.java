package com.accountia.auth.config;

import com.accountia.auth.model.User;
import com.accountia.auth.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import java.util.UUID;

@Configuration
public class DataLoader {
    
    private static final Logger logger = LoggerFactory.getLogger(DataLoader.class);

    @Bean
    CommandLineRunner load(UserRepository userRepository,
                           @Value("${admin.seed.email:admin@accountia.test}") String adminEmail,
                           @Value("${admin.seed.username:admin}") String adminUsername,
                           @Value("${admin.seed.password}") String adminPassword) {
        return args -> {
            if (userRepository.count() == 0) {
                final String finalAdminEmail = adminEmail;
                final String finalAdminUsername = adminUsername;
                
                String finalAdminPassword = adminPassword;
                if (finalAdminPassword == null || finalAdminPassword.trim().isEmpty()) {
                    String generatedPassword = UUID.randomUUID().toString().substring(0, 16);
                    logger.warn("Admin seed password not provided. Generated secure password: {}", generatedPassword);
                    finalAdminPassword = generatedPassword;
                }
                
                BCryptPasswordEncoder enc = new BCryptPasswordEncoder();
                User u = new User();
                u.setEmail(finalAdminEmail);
                u.setUsername(finalAdminUsername);
                u.setFirstName("Admin");
                u.setLastName("User");
                u.setPasswordHash(enc.encode(finalAdminPassword));
                u.setIsActive(true);
                userRepository.save(u);
                logger.info("Admin user created with email: {}", finalAdminEmail);
            }
        };
    }
}
