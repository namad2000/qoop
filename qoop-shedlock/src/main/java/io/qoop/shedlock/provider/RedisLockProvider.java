package io.qoop.shedlock.provider;

import io.qoop.shedlock.properties.ShedLockProperties;
import lombok.extern.slf4j.Slf4j;
import net.javacrumbs.shedlock.core.LockProvider;
import org.springframework.data.redis.connection.RedisConnectionFactory;

/**
 * Factory for creating Redis LockProvider.
 */
@Slf4j
public final class RedisLockProvider {

    private RedisLockProvider() {
        // Private constructor
    }

    /**
     * Creates a Redis LockProvider with the given ConnectionFactory and properties.
     */
    public static LockProvider create(RedisConnectionFactory connectionFactory, ShedLockProperties properties) {
        ShedLockProperties.Redis redisConfig = properties.getRedis();
        String environment = redisConfig.getEnvironment() != null
                ? redisConfig.getEnvironment()
                : "default";
        String keyPrefix = redisConfig.getKeyPrefix();

        return create(connectionFactory, environment, keyPrefix);
    }

    /**
     * Creates a Redis LockProvider with custom environment and key prefix.
     */
    public static LockProvider create(RedisConnectionFactory connectionFactory, String environment, String keyPrefix) {
        log.info("Creating Redis LockProvider with environment: {}, keyPrefix: {}", environment, keyPrefix);

        // Constructor: (RedisConnectionFactory, String, String)
        return new net.javacrumbs.shedlock.provider.redis.spring.RedisLockProvider(connectionFactory, environment, keyPrefix);
    }

    /**
     * Creates a Redis LockProvider with default settings.
     */
    public static LockProvider createDefault(RedisConnectionFactory connectionFactory) {
        return new net.javacrumbs.shedlock.provider.redis.spring.RedisLockProvider(connectionFactory, "default", "job-lock");
    }

    /**
     * Creates a Redis LockProvider with only environment.
     */
    public static LockProvider createWithEnvironment(RedisConnectionFactory connectionFactory, String environment) {
        return new net.javacrumbs.shedlock.provider.redis.spring.RedisLockProvider(connectionFactory, environment, "job-lock");
    }
}