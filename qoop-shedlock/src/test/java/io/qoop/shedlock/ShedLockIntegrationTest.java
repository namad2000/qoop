package io.qoop.shedlock;

import io.qoop.shedlock.config.ShedLockAutoConfiguration;
import io.qoop.shedlock.properties.ShedLockProperties;
import net.javacrumbs.shedlock.core.LockProvider;
import net.javacrumbs.shedlock.spring.annotation.EnableSchedulerLock;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.jdbc.autoconfigure.DataSourceAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.test.context.TestPropertySource;

import javax.sql.DataSource;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

@SpringBootTest(classes = {
        ShedLockIntegrationTest.TestConfig.class,
        ShedLockAutoConfiguration.class
})
@TestPropertySource(properties = {
        "shedlock.provider=database",
        "shedlock.database.table-name=test_shedlock"
})
@DisplayName("ShedLock Integration Tests")
class ShedLockIntegrationTest {

    @Autowired
    private LockProvider lockProvider;

    @Autowired
    private ShedLockProperties properties;

    @Test
    @DisplayName("Should create LockProvider bean successfully")
    void shouldCreateLockProviderBeanSuccessfully() {
        assertThat(lockProvider).isNotNull();
        assertThat(properties).isNotNull();
        assertThat(properties.getProvider()).isEqualTo("database");
        assertThat(properties.getDatabase().getTableName()).isEqualTo("test_shedlock");
    }

    @Configuration
    @EnableAutoConfiguration(exclude = {DataSourceAutoConfiguration.class})
    @EnableScheduling
    @EnableSchedulerLock(defaultLockAtMostFor = "PT30S")
    @Import(ShedLockAutoConfiguration.class)
    static class TestConfig {

        @Bean
        public DataSource dataSource() {
            return mock(DataSource.class);
        }
    }
}