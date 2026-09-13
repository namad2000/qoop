package io.qoop.outbox.job.step;

import io.qoop.outbox.persistence.entity.OutboxEventEntity;
import io.qoop.outbox.persistence.repository.OutboxEventJpaRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.infrastructure.item.ItemReader;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.Iterator;
import java.util.List;

/**
 * ItemReader implementation for reading outbox events in a partitioned manner.
 */
@Slf4j
@Component
@StepScope
public class OutboxReader implements ItemReader<OutboxEventEntity> {

    private final OutboxEventJpaRepository repository;
    private final Integer limit;
    private final Integer partitionIndex;
    private final Integer gridSize;

    private Iterator<OutboxEventEntity> currentBatch;

    public OutboxReader(
            OutboxEventJpaRepository repository,
            @Value("${outbox.chunk-size:100}") Integer limit,
            @Value("#{stepExecutionContext['partitionIndex'] ?: 0}") Integer partitionIndex,
            @Value("#{stepExecutionContext['gridSize'] ?: 1}") Integer gridSize) {

        this.repository = repository;
        this.limit = limit;
        this.partitionIndex = partitionIndex;
        this.gridSize = gridSize;

        log.info("Initialized reader for partitionIndex: {} of gridSize: {}, limit: {}",
                this.partitionIndex, this.gridSize, this.limit);
    }

    @Override
    public OutboxEventEntity read() {
        if (currentBatch == null || !currentBatch.hasNext()) {
            List<OutboxEventEntity> events = repository.findNewForPartition(partitionIndex, gridSize, limit);

            if (events == null || events.isEmpty()) {
                log.debug("No more events found for partitionIndex: {} of gridSize: {}", partitionIndex, gridSize);
                return null;
            }

            currentBatch = events.iterator();
            log.debug("Loaded {} events for partitionIndex: {} of gridSize: {}", events.size(), partitionIndex, gridSize);
        }

        return currentBatch.next();
    }
}