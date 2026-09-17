package io.qoop.outbox.scheduler;

import io.qoop.batch.core.DynamicJobLauncher;
import io.qoop.cluster.NodeIdentity;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.javacrumbs.shedlock.spring.annotation.SchedulerLock;
import org.springframework.batch.core.job.JobExecution;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
@ConditionalOnProperty(prefix = "outbox", name = "publisher-mode", havingValue = "batch", matchIfMissing = true)
public class OutboxJobScheduler {

    private final DynamicJobLauncher dynamicJobLauncher;
    private final NodeIdentity nodeIdentity;

    @Scheduled(fixedDelayString = "${outbox.batch.fixed-delay:2000}")
    @SchedulerLock(name = "${spring.application.name}-outboxJob", lockAtMostFor = "1m", lockAtLeastFor = "5s")
    public void run() {
        log.debug("Attempting to start outboxJob execution on node: {}", nodeIdentity.getNodeId());

        try {
            JobExecution execution = dynamicJobLauncher.runDynamicJob("outboxJob");
            log.info("Successfully started outboxJob with execution ID: {}", execution.getId());
        } catch (Exception e) {
            log.error("Failed to start outboxJob: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to start outboxJob", e);
        }
    }
}