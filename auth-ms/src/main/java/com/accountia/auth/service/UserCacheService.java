package com.accountia.auth.service;

import com.accountia.auth.model.User;
import com.accountia.auth.repository.UserRepository;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class UserCacheService {
    private final UserRepository userRepository;

    public UserCacheService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Cacheable(value = "users", key = "#email")
    public Optional<User> findByEmailCached(String email) {
        return userRepository.findByEmail(email);
    }
}
