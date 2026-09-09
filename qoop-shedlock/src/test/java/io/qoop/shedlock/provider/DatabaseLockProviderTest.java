package io.qoop.shedlock.provider;

import io.qoop.shedlock.properties.ShedLockProperties;
import net.javacrumbs.shedlock.core.LockProvider;
import net.javacrumbs.shedlock.provider.jdbctemplate.JdbcTemplateLockProvider;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import javax.sql.DataSource;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

@DisplayName("Database Lock Provider Tests")
class DatabaseLockProviderTest {

    @Test
    @DisplayName("Should create JdbcTemplateLockProvider with custom table name")
    void shouldCreateJdbcTemplateLockProviderWithCustomTableName() {
        // Given
        DataSource dataSource = mock(DataSource.class);
        ShedLockProperties properties = new ShedLockProperties();
        properties.getDatabase().setTableName("custom_lock_table");

        // When
        LockProvider provider = DatabaseLockProvider.create(dataSource, properties);

        // Then
        assertThat(provider).isInstanceOf(JdbcTemplateLockProvider.class);
    }

    @Test
    @DisplayName("Should create JdbcTemplateLockProvider with default table name")
    void shouldCreateJdbcTemplateLockProviderWithDefaultTableName() {
        // Given
        DataSource dataSource = mock(DataSource.class);
        ShedLockProperties properties = new ShedLockProperties();

        // When
        LockProvider provider = DatabaseLockProvider.create(dataSource, properties);

        // Then
        assertThat(provider).isInstanceOf(JdbcTemplateLockProvider.class);
    }

    @Test
    @DisplayName("Should create JdbcTemplateLockProvider with DataSource")
    void shouldCreateJdbcTemplateLockProviderWithDataSource() {
        // Given
        DataSource dataSource = mock(DataSource.class);
        ShedLockProperties properties = new ShedLockProperties();

        // When
        LockProvider provider = DatabaseLockProvider.create(dataSource, properties);

        // Then
        assertThat(provider).isNotNull();
        assertThat(provider).isInstanceOf(JdbcTemplateLockProvider.class);
    }
}