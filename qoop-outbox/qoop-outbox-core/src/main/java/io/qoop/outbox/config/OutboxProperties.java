package io.qoop.outbox.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Setter
@Getter
@ConfigurationProperties(prefix = "outbox")
public class OutboxProperties {

    private String publisherMode = "batch";
    private int gridSize = 4;
    private int chunkSize = 100;
    private long fixedDelay = 2000;

    private Cleanup cleanup = new Cleanup();
    private Batch batch = new Batch();

    @Setter
    @Getter
    public static class Cleanup {
        private boolean enabled = true;
        private String cron = "0 0 3 * * *";
        private int retentionDays = 1;
    }

    @Setter
    @Getter
    public static class Batch {
        private int gridSize = 4;
        private int chunkSize = 100;
        private long fixedDelay = 2000;
    }
}