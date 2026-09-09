package io.qoop.outbox.scheduler;

import io.qoop.outbox.config.OutboxProperties;
import io.qoop.outbox.persistence.repository.OutboxEventJpaRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.javacrumbs.shedlock.spring.annotation.SchedulerLock;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

/**
 * Scheduler for cleaning up old outbox events.
 *
 * <p>Behavior based on publisher mode:
 * <ul>
 *   <li><b>BATCH mode:</b> Events are marked as SENT after publishing.
 *       Cleanup deletes only SENT events older than threshold.</li>
 *   <li><b>DEBEZIUM mode:</b> Events remain NEW (Debezium reads them via CDC).
 *       Cleanup deletes ALL events (NEW) older than threshold to prevent
 *       infinite accumulation.</li>
 * </ul>
 * </p>
 *
 * <p>Runs daily at 3 AM by default.</p>
 */
@Slf4j
@Component
@RequiredArgsConstructor
@ConditionalOnProperty(
        prefix = "outbox",
        name = "cleanup.enabled",
        havingValue = "true",
        matchIfMissing = true
)
public class OutboxCleanupScheduler {

    private final OutboxEventJpaRepository outboxEventRepository;
    private final OutboxProperties outboxProperties;

    /**
     * Cleans up old outbox events based on publisher mode.
     *
     * <p>In BATCH mode: Deletes only SENT events older than threshold
     * In DEBEZIUM mode: Deletes all events older than threshold</p>
     */
    @Scheduled(cron = "${outbox.cleanup.cron:0 0 3 * * *}")
    @SchedulerLock(name = "outboxCleanup", lockAtMostFor = "10m", lockAtLeastFor = "1m")
    public void cleanup() {
        int retentionDays = getRetentionDays();
        LocalDateTime threshold = LocalDateTime.now().minusDays(retentionDays);
        String publisherMode = outboxProperties.getPublisherMode();

        if ("debezium".equalsIgnoreCase(publisherMode)) {
            // Debezium: events never change status, delete ALL old events
            outboxEventRepository.deleteCreatedBefore(threshold);
            log.info("Cleaned up ALL outbox events created before: {} (DEBEZIUM mode)", threshold);
        } else {
            // Batch: events become SENT after publishing, delete only SENT
            outboxEventRepository.deleteSentBefore(threshold);
            log.info("Cleaned up SENT outbox events sent before: {} (BATCH mode)", threshold);
        }
    }

    /**
     * Gets retention days from properties with null safety.
     */
    private int getRetentionDays() {
        try {
            if (outboxProperties.getCleanup() != null) {
                return outboxProperties.getCleanup().getRetentionDays();
            }
        } catch (Exception e) {
            log.warn("Failed to get retention days from properties, using default: 1", e);
        }
        return 1; // Default retention days
    }
}