package io.qoop.shedlock.config;

import io.qoop.shedlock.properties.ShedLockProperties;
import io.qoop.shedlock.provider.DatabaseLockProvider;
import io.qoop.shedlock.provider.RedisLockProvider;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.javacrumbs.shedlock.core.LockProvider;
import net.javacrumbs.shedlock.spring.annotation.EnableSchedulerLock;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.scheduling.annotation.EnableScheduling;

import javax.sql.DataSource;

@Slf4j
@AutoConfiguration
@EnableScheduling
@EnableSchedulerLock(defaultLockAtMostFor = "${shedlock.default-lock-at-most-for:PT30S}")
@EnableConfigurationProperties(ShedLockProperties.class)
@RequiredArgsConstructor
public class ShedLockAutoConfiguration {

    private final ShedLockProperties properties;

    /**
     * Database LockProvider bean.
     * Active when 'shedlock.provider=database' or not set (default).
     * Only created if no other LockProvider bean exists.
     */
    @Bean
    @ConditionalOnMissingBean(LockProvider.class)
    @ConditionalOnClass(name = "org.springframework.jdbc.core.JdbcTemplate")
    @ConditionalOnProperty(prefix = "shedlock", name = "provider", havingValue = "database", matchIfMissing = true)
    public LockProvider databaseLockProvider(DataSource dataSource) {
        log.info("Creating Database LockProvider with table: {}", properties.getDatabase().getTableName());
        return DatabaseLockProvider.create(dataSource, properties);
    }

    /**
     * Redis LockProvider bean.
     * Active when 'shedlock.provider=redis'.
     * Only created if no other LockProvider bean exists.
     */
    @Bean
    @ConditionalOnMissingBean(LockProvider.class)
    @ConditionalOnClass(name = "org.springframework.data.redis.connection.RedisConnectionFactory")
    @ConditionalOnProperty(prefix = "shedlock", name = "provider", havingValue = "redis")
    public LockProvider redisLockProvider(RedisConnectionFactory connectionFactory) {
        log.info("Creating Redis LockProvider with key prefix: {}", properties.getRedis().getKeyPrefix());
        return RedisLockProvider.create(connectionFactory, properties);
    }
}