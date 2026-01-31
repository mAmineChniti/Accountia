package com.accountia.auth.service;

import com.accountia.auth.model.RefreshToken;
import com.accountia.auth.model.User;
import com.accountia.auth.repository.RefreshTokenRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

@Service
public class RefreshTokenService {

    private final RefreshTokenRepository repository;
    private final long refreshTokenDurationMs;

    public RefreshTokenService(RefreshTokenRepository repository,
                               @Value("${security.refresh.expiration-ms:2592000000}") long refreshTokenDurationMs) {
        this.repository = repository;
        this.refreshTokenDurationMs = refreshTokenDurationMs;
    }

    public RefreshToken createRefreshToken(User user) {
        RefreshToken t = new RefreshToken();
        t.setUser(user);
        t.setToken(UUID.randomUUID().toString());
        t.setExpiryDate(Instant.now().plusMillis(refreshTokenDurationMs));
        return repository.save(t);
    }

    public Optional<RefreshToken> findByToken(String token) {
        return repository.findByToken(token);
    }

    public int deleteByUser(User user) {
        return repository.deleteByUser(user);
    }
}
