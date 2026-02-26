package com.accountia.auth.service;

import com.accountia.auth.model.User;
import com.accountia.auth.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class UserCacheService {
    private static final Logger logger = LoggerFactory.getLogger(UserCacheService.class);
    private final UserRepository userRepository;

    public UserCacheService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public Optional<User> findByIdentifierCached(String identifier) {
        logger.debug("Looking up user by identifier: {}", identifier);
        Optional<User> user = userRepository.findByEmail(identifier);
        if (user.isEmpty()) {
            user = userRepository.findByUsername(identifier);
        }
        logger.debug("User found: {}", user.isPresent());
        return user;
    }
}
