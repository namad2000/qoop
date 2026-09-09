package io.qoop.batch.partition.strategy;

import io.qoop.cluster.NodeIdentity;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.partition.Partitioner;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.Step;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.infrastructure.item.ExecutionContext;
import org.springframework.transaction.PlatformTransactionManager;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Local partitioning strategy for single-node execution.
 *
 * <p>Even with a single node, data is divided into multiple partitions.
 * Each partition is processed locally with chunk-oriented execution.
 *
 * <p>This strategy is useful for:
 * <ul>
 *   <li>Better performance through parallel processing on the same node</li>
 *   <li>Consistent behavior between local and hybrid modes</li>
 *   <li>Easier testing and debugging</li>
 * </ul>
 *
 * <p>Users can provide custom partitioners by using
 * {@link #createPartitionStepWithCustomPartitioner} method.
 */
@Slf4j
@RequiredArgsConstructor
public class LocalPartitionStrategy implements PartitionStrategy {

    private final NodeIdentity nodeIdentity;
    private final int partitionCount;
    private final int chunkSize;
    private final boolean kafkaEnabled;

    @Override
    public Step createPartitionStep(
            String stepName,
            JobRepository jobRepository,
            PlatformTransactionManager transactionManager,
            Step workerStep) {

        log.info("Creating LOCAL partition step: {} with {} partitions", stepName, partitionCount);
        log.info("Node: {}, ChunkSize: {}", nodeIdentity.getNodeId(), chunkSize);

        Partitioner localPartitioner = createLocalPartitioner();

        return new StepBuilder(stepName, jobRepository)
                .partitioner("workerStep", localPartitioner)
                .step(workerStep)
                .gridSize(partitionCount)
                .build();
    }

    /**
     * Creates a partition step with a custom partitioner in local mode.
     * All partitions remain on the same node, but the user controls the partitioning logic.
     *
     * @param stepName           Name of the step
     * @param jobRepository      Spring Batch job repository
     * @param transactionManager Transaction manager
     * @param workerStep         The standard chunk-oriented step
     * @param customPartitioner  User-provided custom partitioner
     * @return Configured Step instance with custom local partitioning
     */
    public Step createPartitionStepWithCustomPartitioner(
            String stepName,
            JobRepository jobRepository,
            PlatformTransactionManager transactionManager,
            Step workerStep,
            Partitioner customPartitioner) {

        log.info("Creating LOCAL partition step with CUSTOM partitioner: {}",
                customPartitioner.getClass().getSimpleName());
        log.info("Node: {}, Partitions: {}", nodeIdentity.getNodeId(), partitionCount);

        return new StepBuilder(stepName, jobRepository)
                .partitioner("workerStep", customPartitioner)
                .step(workerStep)
                .gridSize(partitionCount)
                .build();
    }

    /**
     * Creates the default local partitioner.
     * All partitions are assigned to the current node.
     *
     * @return Default local Partitioner instance
     */
    private Partitioner createLocalPartitioner() {
        return new Partitioner() {
            @Override
            public Map<String, ExecutionContext> partition(int gridSize) {
                Map<String, ExecutionContext> partitions = new ConcurrentHashMap<>();
                String selfId = nodeIdentity.getNodeId();

                log.info("Creating {} local partitions on node: {}", gridSize, selfId);

                for (int i = 0; i < gridSize; i++) {
                    ExecutionContext context = putValue(selfId, i, chunkSize);
                    partitions.put("partition-" + i, context);
                    log.debug("Local partition {} created on node: {}", i, selfId);
                }

                log.info("Created {} local partitions on node: {}", partitions.size(), selfId);
                return partitions;
            }
        };
    }

    @Override
    public boolean isMaster() {
        return true;
    }

    @Override
    public List<String> getWorkerNodes() {
        return List.of(nodeIdentity.getNodeId());
    }

    @Override
    public int getPartitionCount() {
        return partitionCount;
    }

    @Override
    public int getChunkSize() {
        return chunkSize;
    }

    @Override
    public boolean isKafkaEnabled() {
        return kafkaEnabled;
    }
}