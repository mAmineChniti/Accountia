package com.accountia.auth.service;

import com.accountia.auth.dto.UserDTO;
import com.accountia.auth.model.User;
import com.accountia.auth.repository.UserRepository;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class AuthService {
    private final UserRepository userRepository;

    public AuthService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public Optional<UserDTO> login(String email, String password) {
        // For scaffold: accept any password and return user if exists
        return userRepository.findByEmail(email).map(u -> {
            UserDTO dto = new UserDTO();
            dto.setId(u.getId());
            dto.setEmail(u.getEmail());
            dto.setTenantId(u.getTenantId());
            return dto;
        });
    }
}
