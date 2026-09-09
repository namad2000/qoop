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
 *
 * <p>Uses a port-based approach where each partition reads events based on
 * hash partitioning to ensure even distribution across nodes.</p>
 *
 * <p>The reader uses SKIP LOCKED in the underlying query to prevent
 * concurrent processing of the same events across multiple nodes.</p>
 *
 * <p>Partition index and grid size are injected via @Value from the
 * step execution context at runtime.</p>
 *
 * <p>Uses prototype scope so each partition gets its own instance.</p>
 */
@Slf4j
@Component
@StepScope
public class OutboxReader implements ItemReader<OutboxEventEntity> {

    private final OutboxEventJpaRepository repository;
    private final int limit;
    private final int partitionIndex;
    private final int gridSize;

    private Iterator<OutboxEventEntity> currentBatch;

    public OutboxReader(
            OutboxEventJpaRepository repository,
            @Value("${outbox.chunk-size:100}") int limit,
            @Value("#{stepExecutionContext['partitionIndex']}") int partitionIndex,
            @Value("#{stepExecutionContext['gridSize']}") int gridSize) {

        this.repository = repository;
        this.limit = limit;
        this.partitionIndex = partitionIndex;
        this.gridSize = gridSize;

        log.info("Initialized reader for partition: {} of {}, limit: {}",
                partitionIndex, gridSize, limit);
    }

    @Override
    public OutboxEventEntity read() {
        if (currentBatch == null) {
            List<OutboxEventEntity> events = repository.findNewForPartition(partitionIndex, gridSize, limit);
            currentBatch = events.iterator();
            log.debug("Loaded {} events for partition {} of {}", events.size(), partitionIndex, gridSize);
        }
        return currentBatch.hasNext() ? currentBatch.next() : null;
    }
}