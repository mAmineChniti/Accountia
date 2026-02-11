package com.accountia.auth.service;

import org.springframework.cache.annotation.CacheEvict;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.Set;
import java.util.concurrent.TimeUnit;

@Service
public class TokenBlacklistService {

    private final RedisTemplate<String, Object> redisTemplate;
    private static final String BLACKLIST_PREFIX = "token:blacklist:";
    private static final String BLACKLIST_PATTERN = BLACKLIST_PREFIX + "*";
    private static final long TOKEN_TTL_HOURS = 24;

    public TokenBlacklistService(RedisTemplate<String, Object> redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    public void blacklistToken(String token, long expiryTimeInSeconds) {
        String key = BLACKLIST_PREFIX + token;
        long ttlSeconds = Math.min(expiryTimeInSeconds, TOKEN_TTL_HOURS * 3600);
        redisTemplate.opsForValue().set(key, "revoked", ttlSeconds, TimeUnit.SECONDS);
    }

    public boolean isBlacklisted(String token) {
        String key = BLACKLIST_PREFIX + token;
        Boolean exists = redisTemplate.hasKey(key);
        return exists != null && exists;
    }

    public void removeFromBlacklist(String token) {
        String key = BLACKLIST_PREFIX + token;
        redisTemplate.delete(key);
    }

    @CacheEvict(value = "blacklist", allEntries = true)
    public void clearBlacklist() {
        var connectionFactory = redisTemplate.getConnectionFactory();
        if (connectionFactory == null) {
            return;
        }
        Set<String> keys = redisTemplate.keys(BLACKLIST_PATTERN);
        if (keys != null && !keys.isEmpty()) {
            redisTemplate.delete(keys);
        }
    }
}
