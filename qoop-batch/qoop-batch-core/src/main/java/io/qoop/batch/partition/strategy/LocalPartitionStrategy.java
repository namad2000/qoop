package io.qoop.batch.partition.strategy;

import io.qoop.cluster.NodeIdentity;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.partition.Partitioner;
import org.springframework.batch.core.partition.support.TaskExecutorPartitionHandler;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.Step;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.infrastructure.item.ExecutionContext;
import org.springframework.core.task.SimpleAsyncTaskExecutor;
import org.springframework.transaction.PlatformTransactionManager;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Local partitioning strategy for single-node execution.
 */
@Slf4j
@RequiredArgsConstructor
public class LocalPartitionStrategy implements PartitionStrategy {

    private final NodeIdentity nodeIdentity;
    private final int partitionCount;
    private final int chunkSize;
    private final boolean kafkaEnabled;

    @Override
    public Partitioner createDefaultPartitioner() {
        return gridSize -> {
            Map<String, ExecutionContext> partitions = new ConcurrentHashMap<>();
            String selfId = nodeIdentity.getNodeId();

            log.info("Creating {} local partitions on node: {}", gridSize, selfId);

            for (int i = 0; i < gridSize; i++) {
                ExecutionContext context = putValue(selfId, i, chunkSize);
                partitions.put("partition-" + i, context);
            }

            return partitions;
        };
    }

    @Override
    public Step createPartitionStepWithPartitioner(
            String stepName,
            JobRepository jobRepository,
            PlatformTransactionManager transactionManager,
            Step workerStep,
            Partitioner partitioner) {

        log.info("Creating LOCAL partition step: {} with {} partitions", stepName, partitionCount);

        TaskExecutorPartitionHandler partitionHandler = new TaskExecutorPartitionHandler();
        partitionHandler.setStep(workerStep);
        partitionHandler.setGridSize(partitionCount);

        SimpleAsyncTaskExecutor taskExecutor = new SimpleAsyncTaskExecutor("local-partition-");
        taskExecutor.setConcurrencyLimit(partitionCount);
        partitionHandler.setTaskExecutor(taskExecutor);

        try {
            partitionHandler.afterPropertiesSet();
        } catch (Exception e) {
            log.error("Failed to initialize partition handler for step: {}", stepName, e);
        }

        return new StepBuilder(stepName, jobRepository)
                .partitioner("workerStep", partitioner)
                .partitionHandler(partitionHandler)
                .build();
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