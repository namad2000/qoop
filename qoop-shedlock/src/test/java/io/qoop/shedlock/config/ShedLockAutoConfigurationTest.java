package io.qoop.shedlock.config;

import io.qoop.shedlock.properties.ShedLockProperties;
import net.javacrumbs.shedlock.core.LockConfiguration;
import net.javacrumbs.shedlock.core.LockProvider;
import net.javacrumbs.shedlock.core.SimpleLock;
import net.javacrumbs.shedlock.provider.jdbctemplate.JdbcTemplateLockProvider;
import net.javacrumbs.shedlock.provider.redis.spring.RedisLockProvider;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.jdbc.core.JdbcTemplate;

import javax.sql.DataSource;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

@DisplayName("ShedLock Auto Configuration Tests")
class ShedLockAutoConfigurationTest {

    private ApplicationContextRunner contextRunner;

    @BeforeEach
    void setUp() {
        contextRunner = new ApplicationContextRunner()
                .withConfiguration(AutoConfigurations.of(ShedLockAutoConfiguration.class));
    }

    // ==================== DATABASE PROVIDER TESTS ====================

    @Test
    @DisplayName("Should create Database LockProvider by default when provider is not set")
    void shouldCreateDatabaseLockProviderByDefault() {
        contextRunner
                .withUserConfiguration(TestDatabaseConfig.class)
                .run(context -> {
                    assertThat(context).hasSingleBean(LockProvider.class);
                    assertThat(context.getBean(LockProvider.class))
                            .isInstanceOf(JdbcTemplateLockProvider.class);
                });
    }

    @Test
    @DisplayName("Should create Database LockProvider when provider is 'database'")
    void shouldCreateDatabaseLockProviderWhenProviderIsDatabase() {
        contextRunner
                .withUserConfiguration(TestDatabaseConfig.class)
                .withPropertyValues(
                        "shedlock.provider=database"
                )
                .run(context -> {
                    assertThat(context).hasSingleBean(LockProvider.class);
                    assertThat(context.getBean(LockProvider.class))
                            .isInstanceOf(JdbcTemplateLockProvider.class);
                });
    }

    @Test
    @DisplayName("Should create Database LockProvider with custom table name")
    void shouldCreateDatabaseLockProviderWithCustomTableName() {
        contextRunner
                .withUserConfiguration(TestDatabaseConfig.class)
                .withPropertyValues(
                        "shedlock.provider=database",
                        "shedlock.database.table-name=custom_lock_table"
                )
                .run(context -> {
                    assertThat(context).hasSingleBean(LockProvider.class);

                    ShedLockProperties properties = context.getBean(ShedLockProperties.class);
                    assertThat(properties.getDatabase().getTableName()).isEqualTo("custom_lock_table");
                });
    }

    // ==================== REDIS PROVIDER TESTS ====================

    @Test
    @DisplayName("Should create Redis LockProvider when provider is 'redis'")
    void shouldCreateRedisLockProviderWhenProviderIsRedis() {
        contextRunner
                .withUserConfiguration(TestRedisConfig.class)
                .withPropertyValues(
                        "shedlock.provider=redis",
                        "shedlock.redis.key-prefix=test-shedlock",
                        "shedlock.redis.environment=test-env"
                )
                .run(context -> {
                    assertThat(context).hasSingleBean(LockProvider.class);
                    assertThat(context.getBean(LockProvider.class))
                            .isInstanceOf(RedisLockProvider.class);
                });
    }

    @Test
    @DisplayName("Should create Redis LockProvider with custom key prefix")
    void shouldCreateRedisLockProviderWithCustomKeyPrefix() {
        contextRunner
                .withUserConfiguration(TestRedisConfig.class)
                .withPropertyValues(
                        "shedlock.provider=redis",
                        "shedlock.redis.key-prefix=custom-prefix",
                        "shedlock.redis.environment=prod"
                )
                .run(context -> {
                    assertThat(context).hasSingleBean(LockProvider.class);
                    assertThat(context.getBean(LockProvider.class))
                            .isInstanceOf(RedisLockProvider.class);

                    ShedLockProperties properties = context.getBean(ShedLockProperties.class);
                    assertThat(properties.getRedis().getKeyPrefix()).isEqualTo("custom-prefix");
                    assertThat(properties.getRedis().getEnvironment()).isEqualTo("prod");
                });
    }

    @Test
    @DisplayName("Should create Redis LockProvider with environment as context")
    void shouldCreateRedisLockProviderWithEnvironmentAsContext() {
        contextRunner
                .withUserConfiguration(TestRedisConfig.class)
                .withPropertyValues(
                        "shedlock.provider=redis",
                        "shedlock.redis.environment=my-app"
                )
                .run(context -> {
                    assertThat(context).hasSingleBean(LockProvider.class);
                    assertThat(context.getBean(LockProvider.class))
                            .isInstanceOf(RedisLockProvider.class);

                    ShedLockProperties properties = context.getBean(ShedLockProperties.class);
                    assertThat(properties.getRedis().getEnvironment()).isEqualTo("my-app");
                });
    }

    @Test
    @DisplayName("Should create Redis LockProvider with no environment")
    void shouldCreateRedisLockProviderWithNoEnvironment() {
        contextRunner
                .withUserConfiguration(TestRedisConfig.class)
                .withPropertyValues(
                        "shedlock.provider=redis",
                        "shedlock.redis.key-prefix=fallback-prefix"
                )
                .run(context -> {
                    assertThat(context).hasSingleBean(LockProvider.class);
                    assertThat(context.getBean(LockProvider.class))
                            .isInstanceOf(RedisLockProvider.class);

                    ShedLockProperties properties = context.getBean(ShedLockProperties.class);
                    assertThat(properties.getRedis().getKeyPrefix()).isEqualTo("fallback-prefix");
                });
    }

    // ==================== PROPERTY TESTS ====================

    @Test
    @DisplayName("Should use default property values when not configured")
    void shouldUseDefaultPropertyValues() {
        contextRunner
                .withUserConfiguration(TestDatabaseConfig.class)
                .run(context -> {
                    assertThat(context).hasSingleBean(ShedLockProperties.class);

                    ShedLockProperties properties = context.getBean(ShedLockProperties.class);

                    assertThat(properties.getProvider()).isEqualTo("database");
                    assertThat(properties.getDefaultLockAtMostFor()).isEqualTo("PT30S");
                    assertThat(properties.getDatabase().getTableName()).isEqualTo("shedlock");
                    assertThat(properties.getRedis().getKeyPrefix()).isEqualTo("shedlock");
                });
    }

    @Test
    @DisplayName("Should create LockProvider with custom default lock time")
    void shouldCreateLockProviderWithCustomDefaultLockTime() {
        contextRunner
                .withUserConfiguration(TestDatabaseConfig.class)
                .withPropertyValues(
                        "shedlock.default-lock-at-most-for=PT1H"
                )
                .run(context -> {
                    ShedLockProperties properties = context.getBean(ShedLockProperties.class);
                    assertThat(properties.getDefaultLockAtMostFor()).isEqualTo("PT1H");
                });
    }

    // ==================== EXISTING BEAN TESTS ====================

    @Test
    @DisplayName("Should not create new LockProvider when one already exists")
    void shouldNotCreateLockProviderWhenAlreadyExists() {
        contextRunner
                .withUserConfiguration(TestExistingLockProviderConfig.class)
                .withPropertyValues(
                        "shedlock.provider=database"
                )
                .run(context -> {
                    // فقط یک Bean از نوع LockProvider باید وجود داشته باشد (customLockProvider)
                    assertThat(context).hasSingleBean(LockProvider.class);
                    assertThat(context.getBean(LockProvider.class))
                            .isInstanceOf(CustomLockProvider.class);
                });
    }

    @Test
    @DisplayName("Should respect existing LockProvider even when provider is redis")
    void shouldRespectExistingLockProviderEvenWhenProviderIsRedis() {
        contextRunner
                .withUserConfiguration(TestExistingLockProviderConfig.class)
                .withPropertyValues(
                        "shedlock.provider=redis"
                )
                .run(context -> {
                    assertThat(context).hasSingleBean(LockProvider.class);
                    assertThat(context.getBean(LockProvider.class))
                            .isInstanceOf(CustomLockProvider.class);
                });
    }

    // ==================== TEST CONFIGURATIONS ====================

    @Configuration
    static class TestDatabaseConfig {

        @Bean
        public DataSource dataSource() {
            return mock(DataSource.class);
        }

        @Bean
        public JdbcTemplate jdbcTemplate(DataSource dataSource) {
            return new JdbcTemplate(dataSource);
        }
    }

    @Configuration
    static class TestRedisConfig {

        @Bean
        public DataSource dataSource() {
            return mock(DataSource.class);
        }

        @Bean
        public RedisConnectionFactory redisConnectionFactory() {
            return mock(RedisConnectionFactory.class);
        }
    }

    @Configuration
    static class TestExistingLockProviderConfig {

        @Bean
        public DataSource dataSource() {
            return mock(DataSource.class);
        }

        @Bean
        public LockProvider customLockProvider() {
            return new CustomLockProvider();
        }
    }

    static class CustomLockProvider implements LockProvider {

        @Override
        public Optional<SimpleLock> lock(LockConfiguration lockConfiguration) {
            return Optional.of(() -> {});
        }
    }
}