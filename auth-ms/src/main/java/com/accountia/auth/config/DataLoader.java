package com.accountia.auth.config;

import com.accountia.auth.model.User;
import com.accountia.auth.repository.UserRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

@Configuration
public class DataLoader {

    @Bean
    CommandLineRunner load(UserRepository userRepository) {
        return args -> {
            if (userRepository.count() == 0) {
                BCryptPasswordEncoder enc = new BCryptPasswordEncoder();
                User u = new User();
                u.setEmail("admin@accountia.test");
                u.setTenantId("tenant_default");
                u.setPasswordHash(enc.encode("password"));
                userRepository.save(u);
            }
        };
    }
}
