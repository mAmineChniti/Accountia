package org.springframework.boot.autoconfigure.data.redis;

import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.boot.data.redis.autoconfigure.DataRedisReactiveAutoConfiguration;

@Configuration
@Import(DataRedisReactiveAutoConfiguration.class)
public class RedisReactiveAutoConfiguration {
    // shim to satisfy Spring Cloud Gateway's reference to
    // org.springframework.boot.autoconfigure.data.redis.RedisReactiveAutoConfiguration
}
