package com.accountia.auth.service;

import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.concurrent.TimeUnit;

@Service
public class RateLimitingService {

    @Nullable
    private final StringRedisTemplate stringRedisTemplate;
    
    private static final String LOGIN_ATTEMPT_PREFIX = "ratelimit:login:";
    private static final int MAX_ATTEMPTS = 5;
    private static final long WINDOW_SIZE_MINUTES = 15;
    private static final long WINDOW_SIZE_SECONDS = WINDOW_SIZE_MINUTES * 60;

    public RateLimitingService(@Nullable StringRedisTemplate stringRedisTemplate) {
        this.stringRedisTemplate = stringRedisTemplate;
    }

    public boolean isRateLimited(String email) {
        if (email == null) {
            throw new IllegalArgumentException("email must not be null");
        }
        if (stringRedisTemplate == null) {
            return false;
        }
        String key = LOGIN_ATTEMPT_PREFIX + email;
        String attempts = stringRedisTemplate.opsForValue().get(key);
        return attempts != null && Integer.parseInt(attempts) >= MAX_ATTEMPTS;
    }

    public void recordFailedAttempt(String email) {
        if (email == null) {
            throw new IllegalArgumentException("email must not be null");
        }
        if (stringRedisTemplate == null) {
            return;
        }
        String key = LOGIN_ATTEMPT_PREFIX + email;

        DefaultRedisScript<Long> script = new DefaultRedisScript<>();
        script.setResultType(Long.class);
        script.setScriptText(
                "local current = redis.call('INCR', KEYS[1]) " +
                "local ttl = redis.call('TTL', KEYS[1]) " +
                "if ttl == -1 then redis.call('EXPIRE', KEYS[1], ARGV[1]) end " +
                "return current"
        );

        stringRedisTemplate.execute(script, List.of(key), String.valueOf(WINDOW_SIZE_SECONDS));
    }

    public void resetAttempts(String email) {
        if (stringRedisTemplate == null) {
            return;
        }
        String key = LOGIN_ATTEMPT_PREFIX + email;
        stringRedisTemplate.delete(key);
    }

    public int getAttemptCount(String email) {
        if (stringRedisTemplate == null) {
            return 0;
        }
        String key = LOGIN_ATTEMPT_PREFIX + email;
        String attempts = stringRedisTemplate.opsForValue().get(key);
        return attempts != null ? Integer.parseInt(attempts) : 0;
    }

    public long getRemainingLockTime(String email) {
        if (stringRedisTemplate == null) {
            return -2;
        }
        String key = LOGIN_ATTEMPT_PREFIX + email;
        Long ttl = stringRedisTemplate.getExpire(key, TimeUnit.SECONDS);
        return ttl != null ? ttl : -2;
    }
}
