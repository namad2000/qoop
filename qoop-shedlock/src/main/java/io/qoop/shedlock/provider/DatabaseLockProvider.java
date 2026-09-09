package io.qoop.shedlock.provider;

import io.qoop.shedlock.properties.ShedLockProperties;
import lombok.extern.slf4j.Slf4j;
import net.javacrumbs.shedlock.core.LockProvider;
import net.javacrumbs.shedlock.provider.jdbctemplate.JdbcTemplateLockProvider;
import org.springframework.jdbc.core.JdbcTemplate;

import javax.sql.DataSource;

/**
 * Factory for creating Database LockProvider.
 */
@Slf4j
public final class DatabaseLockProvider {

    private DatabaseLockProvider() {
        // Private constructor
    }

    /**
     * Creates a Database LockProvider with the given DataSource and properties.
     *
     * @param dataSource DataSource for database connection
     * @param properties ShedLock properties
     * @return LockProvider instance
     */
    public static LockProvider create(DataSource dataSource, ShedLockProperties properties) {
        ShedLockProperties.Database dbConfig = properties.getDatabase();

        log.info("Creating Database LockProvider with table: {}", dbConfig.getTableName());

        return new JdbcTemplateLockProvider(
                JdbcTemplateLockProvider.Configuration.builder()
                        .withJdbcTemplate(new JdbcTemplate(dataSource))
                        .withTableName(dbConfig.getTableName())
                        .build()
        );
    }
}