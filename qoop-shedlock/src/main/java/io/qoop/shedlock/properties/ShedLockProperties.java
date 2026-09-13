package io.qoop.shedlock.properties;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Configuration properties for ShedLock.
 *
 * <p>Supports both Database and Redis lock providers.</p>
 *
 * <p>Example configuration:
 * <pre>
 * shedlock:
 *   provider: database          # database | redis
 *   default-lock-at-most-for: PT30S
 *   database:
 *     table-name: shedlock
 *   redis:
 *     key-prefix: shedlock
 * </pre>
 * </p>
 */
@Setter
@Getter
@ConfigurationProperties(prefix = "shedlock")
public class ShedLockProperties {

    /**
     * Lock provider type: database or redis
     */
    private String provider = "redis";

    /**
     * Default lock time (e.g., PT30S, 30s, 30 seconds)
     */
    private String defaultLockAtMostFor = "PT30S";

    /**
     * Database-specific configuration
     */
    private Database database = new Database();

    /**
     * Redis-specific configuration
     */
    private Redis redis = new Redis();

    @Setter
    @Getter
    public static class Database {
        /**
         * Table name for shedlock (default: shedlock)
         */
        private String tableName = "shedlock";

        /**
         * Whether to use DB time (default: true)
         */
        private boolean useDbTime = true;
    }

    @Setter
    @Getter
    public static class Redis {
        /**
         * Key prefix for redis lock (default: shedlock)
         */
        private String keyPrefix = "shedlock";

        /**
         * Redis environment (default: null)
         */
        private String environment;
    }
}