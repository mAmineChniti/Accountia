package com.accountia.auth.service;

import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

@Service
public class TokenCacheService {

    private final RedisTemplate<String, Object> redisTemplate;
    private static final String TOKEN_CACHE_PREFIX = "token:validated:";
    private static final long DEFAULT_TOKEN_TTL_MINUTES = 15;

    public TokenCacheService(RedisTemplate<String, Object> redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    public void cacheToken(String tokenId, Object claims, long ttlMinutes) {
        String key = TOKEN_CACHE_PREFIX + tokenId;
        redisTemplate.opsForValue().set(key, claims, ttlMinutes, TimeUnit.MINUTES);
    }

    public Object getCachedToken(String tokenId) {
        String key = TOKEN_CACHE_PREFIX + tokenId;
        return redisTemplate.opsForValue().get(key);
    }

    public boolean isTokenCached(String tokenId) {
        String key = TOKEN_CACHE_PREFIX + tokenId;
        Boolean exists = redisTemplate.hasKey(key);
        return exists != null && exists;
    }

    public void invalidateToken(String tokenId) {
        String key = TOKEN_CACHE_PREFIX + tokenId;
        redisTemplate.delete(key);
    }

    public void cacheTokenWithTtl(String tokenId, Object claims, long ttlSeconds) {
        String key = TOKEN_CACHE_PREFIX + tokenId;
        redisTemplate.opsForValue().set(key, claims, ttlSeconds, TimeUnit.SECONDS);
    }
}
