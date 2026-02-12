package com.accountia.auth.service;

import com.accountia.auth.dto.UserDTO;
import com.accountia.auth.dto.RegisterDTO;
import com.accountia.auth.model.User;
import com.accountia.auth.model.Role;
import com.accountia.auth.repository.UserRepository;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.stereotype.Service;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import java.util.Optional;

@Service
public class AuthService {
    private final UserRepository userRepository;
    private final UserCacheService userCacheService;
    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    public AuthService(UserRepository userRepository, UserCacheService userCacheService) {
        this.userRepository = userRepository;
        this.userCacheService = userCacheService;
    }

    public Optional<UserDTO> login(String email, String password) {
        return userCacheService.findByEmailCached(email)
                .filter(u -> passwordEncoder.matches(password, u.getPasswordHash()))
                .map(u -> {
                    UserDTO dto = new UserDTO();
                    dto.setId(u.getId());
                    dto.setEmail(u.getEmail());
                    dto.setTenantId(u.getTenantId());
                    return dto;
                });
    }

    public Optional<User> findByEmail(String email) {
        return userRepository.findByEmail(email);
    }

    @CacheEvict(value = "users", key = "#dto.email")
    public UserDTO register(RegisterDTO dto) {
        User u = new User();
        u.setEmail(dto.getEmail());
        u.setUsername(dto.getEmail().split("@")[0]);
        u.setTenantId(dto.getTenantId());
        u.setRole(Role.CLIENT);
        u.setPasswordHash(passwordEncoder.encode(dto.getPassword()));
        User saved = userRepository.save(u);
        UserDTO out = new UserDTO();
        out.setId(saved.getId());
        out.setEmail(saved.getEmail());
        out.setTenantId(saved.getTenantId());
        return out;
    }

    @CacheEvict(value = "users", key = "#email")
    public void clearUserCache(String email) {
    }
}
