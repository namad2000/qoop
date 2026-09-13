package io.qoop.shedlock.config;

import io.qoop.shedlock.properties.ShedLockProperties;
import io.qoop.shedlock.provider.DatabaseLockProvider;
import io.qoop.shedlock.provider.RedisLockProvider;
import lombok.extern.slf4j.Slf4j;
import net.javacrumbs.shedlock.core.LockProvider;
import net.javacrumbs.shedlock.provider.jdbctemplate.JdbcTemplateLockProvider;
import net.javacrumbs.shedlock.spring.annotation.EnableSchedulerLock;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.scheduling.annotation.EnableScheduling;

import javax.sql.DataSource;

@Slf4j
@AutoConfiguration
@EnableScheduling
@EnableSchedulerLock(defaultLockAtMostFor = "${shedlock.default-lock-at-most-for:PT30S}")
@EnableConfigurationProperties(ShedLockProperties.class)
public class ShedLockAutoConfiguration {

    /**
     * Redis Lock Provider Infrastructure
     * Active when 'shedlock.provider' is set to 'redis' (or default).
     */
    @Configuration(proxyBeanMethods = false)
    @ConditionalOnClass(name = "net.javacrumbs.shedlock.provider.redis.spring.RedisLockProvider")
    @ConditionalOnProperty(prefix = "shedlock", name = "provider", havingValue = "redis", matchIfMissing = true)
    public static class RedisInfrastructureConfiguration {

        @Bean
        @ConditionalOnMissingBean(LockProvider.class)
        public LockProvider redisLockProvider(RedisConnectionFactory connectionFactory, ShedLockProperties properties) {
            log.info("Creating Redis LockProvider with key prefix: {}", properties.getRedis().getKeyPrefix());
            return RedisLockProvider.create(connectionFactory, properties);
        }
    }

    /**
     * Database Lock Provider Infrastructure
     * Active when 'shedlock.provider' is set to 'database'.
     */
    @Configuration(proxyBeanMethods = false)
    @ConditionalOnClass(JdbcTemplateLockProvider.class)
    @ConditionalOnProperty(prefix = "shedlock", name = "provider", havingValue = "database")
    public static class DatabaseInfrastructureConfiguration {

        @Bean
        @ConditionalOnMissingBean(LockProvider.class)
        public LockProvider databaseLockProvider(DataSource dataSource, ShedLockProperties properties) {
            log.info("Creating Database LockProvider with table: {}", properties.getDatabase().getTableName());
            return DatabaseLockProvider.create(dataSource, properties);
        }
    }
}