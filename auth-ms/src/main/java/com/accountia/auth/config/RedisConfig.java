package com.accountia.auth.config;

import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializationContext;
import org.springframework.data.redis.serializer.StringRedisSerializer;

import java.time.Duration;

/**
 * Redis configuration for Auth Service.
 * Used for caching token validation results and user session data.
 * 
 * WHY REDIS IS USED:
 * 1. Token Cache: Reduces calls to Keycloak for JWT validation by caching valid tokens
 * 2. Session Data: Stores user session information across multiple service replicas
 * 3. Rate Limiting: Can be used to track and limit authentication attempts
 * 4. Blacklist: Store revoked tokens for immediate invalidation
 * 
 * Note: This configuration only activates when Redis classes and beans are available.
 * In CI environments where RedisAutoConfiguration is excluded, these beans won't be created.
 */
@Configuration
@EnableCaching
@ConditionalOnClass(RedisConnectionFactory.class)
@ConditionalOnBean(RedisConnectionFactory.class)
public class RedisConfig {

    /**
     * Redis template for storing generic objects.
     * Uses JSON serialization for values and String for keys.
     */
    @Bean
    public RedisTemplate<String, Object> redisTemplate(RedisConnectionFactory connectionFactory) {
        RedisTemplate<String, Object> template = new RedisTemplate<>();
        template.setConnectionFactory(connectionFactory);
        template.setKeySerializer(new StringRedisSerializer());
        template.setHashKeySerializer(new StringRedisSerializer());
        template.setValueSerializer(new GenericJackson2JsonRedisSerializer());
        template.setHashValueSerializer(new GenericJackson2JsonRedisSerializer());
        template.afterPropertiesSet();
        return template;
    }

    /**
     * Cache manager configuration with different TTLs for different cache types.
     */
    @Bean
    public CacheManager cacheManager(RedisConnectionFactory connectionFactory) {
        RedisCacheConfiguration defaultConfig = RedisCacheConfiguration.defaultCacheConfig()
            .entryTtl(Duration.ofMinutes(30))
            .serializeKeysWith(RedisSerializationContext.SerializationPair.fromSerializer(new StringRedisSerializer()))
            .serializeValuesWith(RedisSerializationContext.SerializationPair.fromSerializer(new GenericJackson2JsonRedisSerializer()))
            .disableCachingNullValues();

        return RedisCacheManager.builder(connectionFactory)
            .cacheDefaults(defaultConfig)
            // Token cache - shorter TTL as tokens expire
            .withCacheConfiguration("tokens", 
                defaultConfig.entryTtl(Duration.ofMinutes(15)))
            // User cache - moderate TTL
            .withCacheConfiguration("users", 
                defaultConfig.entryTtl(Duration.ofHours(1)))
            // Blacklisted tokens - longer TTL, should match max token lifetime
            .withCacheConfiguration("blacklist", 
                defaultConfig.entryTtl(Duration.ofHours(24)))
            .build();
    }
}
