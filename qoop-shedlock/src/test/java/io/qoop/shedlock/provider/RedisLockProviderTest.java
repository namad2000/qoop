package io.qoop.shedlock.provider;

import io.qoop.shedlock.properties.ShedLockProperties;
import net.javacrumbs.shedlock.core.LockProvider;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.data.redis.connection.RedisConnectionFactory;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

@DisplayName("Redis Lock Provider Tests")
class RedisLockProviderTest {

    @Test
    @DisplayName("Should create RedisLockProvider with properties and environment")
    void shouldCreateRedisLockProviderWithPropertiesAndEnvironment() {
        // Given
        RedisConnectionFactory connectionFactory = mock(RedisConnectionFactory.class);
        ShedLockProperties properties = new ShedLockProperties();
        properties.getRedis().setKeyPrefix("custom-prefix");
        properties.getRedis().setEnvironment("prod-env");

        // When
        LockProvider provider = RedisLockProvider.create(connectionFactory, properties);

        // Then
        assertThat(provider).isInstanceOf(net.javacrumbs.shedlock.provider.redis.spring.RedisLockProvider.class);
    }

    @Test
    @DisplayName("Should create RedisLockProvider with properties and no environment")
    void shouldCreateRedisLockProviderWithPropertiesAndNoEnvironment() {
        // Given
        RedisConnectionFactory connectionFactory = mock(RedisConnectionFactory.class);
        ShedLockProperties properties = new ShedLockProperties();
        properties.getRedis().setKeyPrefix("custom-prefix");
        properties.getRedis().setEnvironment(null);

        // When
        LockProvider provider = RedisLockProvider.create(connectionFactory, properties);

        // Then
        assertThat(provider).isInstanceOf(net.javacrumbs.shedlock.provider.redis.spring.RedisLockProvider.class);
    }

    @Test
    @DisplayName("Should create RedisLockProvider with environment and key prefix")
    void shouldCreateRedisLockProviderWithEnvironmentAndKeyPrefix() {
        // Given
        RedisConnectionFactory connectionFactory = mock(RedisConnectionFactory.class);
        String environment = "test-env";
        String keyPrefix = "test-prefix";

        // When
        LockProvider provider = RedisLockProvider.create(connectionFactory, environment, keyPrefix);

        // Then
        assertThat(provider).isInstanceOf(net.javacrumbs.shedlock.provider.redis.spring.RedisLockProvider.class);
    }

    @Test
    @DisplayName("Should create RedisLockProvider with only environment")
    void shouldCreateRedisLockProviderWithOnlyEnvironment() {
        // Given
        RedisConnectionFactory connectionFactory = mock(RedisConnectionFactory.class);
        String environment = "test-env";

        // When
        LockProvider provider = RedisLockProvider.createWithEnvironment(connectionFactory, environment);

        // Then
        assertThat(provider).isInstanceOf(net.javacrumbs.shedlock.provider.redis.spring.RedisLockProvider.class);
    }

    @Test
    @DisplayName("Should create RedisLockProvider with default settings")
    void shouldCreateRedisLockProviderWithDefaultSettings() {
        // Given
        RedisConnectionFactory connectionFactory = mock(RedisConnectionFactory.class);

        // When
        LockProvider provider = RedisLockProvider.createDefault(connectionFactory);

        // Then
        assertThat(provider).isInstanceOf(net.javacrumbs.shedlock.provider.redis.spring.RedisLockProvider.class);
    }

    @Test
    @DisplayName("Should use keyPrefix from properties when environment is null")
    void shouldUseKeyPrefixFromPropertiesWhenEnvironmentIsNull() {
        // Given
        RedisConnectionFactory connectionFactory = mock(RedisConnectionFactory.class);
        ShedLockProperties properties = new ShedLockProperties();
        properties.getRedis().setKeyPrefix("my-prefix");
        properties.getRedis().setEnvironment(null);

        // When
        LockProvider provider = RedisLockProvider.create(connectionFactory, properties);

        // Then
        assertThat(provider).isInstanceOf(net.javacrumbs.shedlock.provider.redis.spring.RedisLockProvider.class);
    }

    @Test
    @DisplayName("Should use environment from properties when not null")
    void shouldUseEnvironmentFromPropertiesWhenNotNull() {
        // Given
        RedisConnectionFactory connectionFactory = mock(RedisConnectionFactory.class);
        ShedLockProperties properties = new ShedLockProperties();
        properties.getRedis().setKeyPrefix("my-prefix");
        properties.getRedis().setEnvironment("my-env");

        // When
       LockProvider provider = RedisLockProvider.create(connectionFactory, properties);

        // Then
        assertThat(provider).isInstanceOf(net.javacrumbs.shedlock.provider.redis.spring.RedisLockProvider.class);
    }
}