package com.accountia.auth.service;

import com.accountia.auth.dto.TokenResponse;
import com.accountia.auth.model.RefreshToken;
import com.accountia.auth.model.User;
import com.accountia.auth.repository.RefreshTokenRepository;
import com.accountia.auth.util.JwtUtil;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

@Service
public class RefreshTokenService {

    private final RefreshTokenRepository repository;
    private final JwtUtil jwtUtil;
    private final long refreshTokenDurationMs;
    private final long accessTokenDurationMs;

    public RefreshTokenService(RefreshTokenRepository repository,
                               JwtUtil jwtUtil,
                               @Value("${security.refresh.expiration-ms:2592000000}") long refreshTokenDurationMs,
                               @Value("${security.access.expiration-ms:900000}") long accessTokenDurationMs) {
        this.repository = repository;
        this.jwtUtil = jwtUtil;
        this.refreshTokenDurationMs = refreshTokenDurationMs;
        this.accessTokenDurationMs = accessTokenDurationMs;
    }

    public RefreshToken createRefreshToken(User user) {
        RefreshToken t = new RefreshToken();
        t.setUser(user);
        t.setToken(UUID.randomUUID().toString());
        t.setExpiryDate(Instant.now().plusMillis(refreshTokenDurationMs));
        return repository.save(t);
    }

    public TokenResponse createSession(User user) {
        RefreshToken refreshToken = createRefreshToken(user);
        Map<String, Object> claims = Map.of(
            "email", user.getEmail(),
            "username", user.getUsername(),
            "userId", user.getId()
        );
        String accessToken = jwtUtil.generateTokenWithClaims(user.getEmail(), claims);
        Instant now = Instant.now();
        return new TokenResponse(
            accessToken,
            now.plusMillis(accessTokenDurationMs),
            refreshToken.getToken(),
            refreshToken.getExpiryDate()
        );
    }

    @Cacheable(value = "refreshTokens", key = "#token")
    public Optional<RefreshToken> findByToken(String token) {
        return repository.findByToken(token);
    }

    @Transactional
    @CacheEvict(value = "refreshTokens", allEntries = true)
    public int deleteByUser(User user) {
        return repository.deleteByUser(user);
    }

    @Transactional
    @CacheEvict(value = "refreshTokens", key = "#token")
    public void invalidateToken(String token) {
        repository.deleteByToken(token);
    }
}
