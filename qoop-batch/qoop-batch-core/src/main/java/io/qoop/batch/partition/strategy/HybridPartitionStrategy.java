package io.qoop.batch.partition.strategy;

import io.qoop.cluster.ClusterNodeCounter;
import io.qoop.cluster.ClusterRoleDetector;
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
import java.util.stream.Collectors;

/**
 * Hybrid partitioning strategy for multi-node cluster execution.
 */
@Slf4j
@RequiredArgsConstructor
public class HybridPartitionStrategy implements PartitionStrategy {

    private final NodeIdentity nodeIdentity;
    private final ClusterNodeCounter nodeCounter;
    private final ClusterRoleDetector roleDetector;
    private final int partitionCount;
    private final int chunkSize;
    private final boolean kafkaEnabled;

    @Override
    public Step createPartitionStep(
            String stepName,
            JobRepository jobRepository,
            PlatformTransactionManager transactionManager,
            Step workerStep) {

        log.info("Creating HYBRID partition step: {} with {} partitions", stepName, partitionCount);

        if (!isMaster()) {
            log.info("Worker mode for node: {} - executing local partitions", nodeIdentity.getNodeId());
            return workerStep;
        }

        Partitioner partitioner = createClusterAwarePartitioner();

        TaskExecutorPartitionHandler partitionHandler = new TaskExecutorPartitionHandler();
        partitionHandler.setStep(workerStep);
        partitionHandler.setGridSize(partitionCount);

        SimpleAsyncTaskExecutor taskExecutor = new SimpleAsyncTaskExecutor("hybrid-partition-");
        taskExecutor.setConcurrencyLimit(partitionCount);
        partitionHandler.setTaskExecutor(taskExecutor);

        try {
            partitionHandler.afterPropertiesSet();
        } catch (Exception e) {
            log.error("Failed to initialize hybrid partition handler for step: {}", stepName, e);
        }

        return new StepBuilder(stepName, jobRepository)
                .partitioner("workerStep", partitioner)
                .partitionHandler(partitionHandler)
                .build();
    }

    @Override
    public Step createPartitionStepWithCustomPartitioner(
            String stepName,
            JobRepository jobRepository,
            PlatformTransactionManager transactionManager,
            Step workerStep,
            Partitioner customPartitioner) {

        log.info("Creating HYBRID partition step with CUSTOM partitioner: {}",
                customPartitioner.getClass().getSimpleName());

        if (!isMaster()) {
            return workerStep;
        }

        TaskExecutorPartitionHandler partitionHandler = new TaskExecutorPartitionHandler();
        partitionHandler.setStep(workerStep);
        partitionHandler.setGridSize(partitionCount);

        SimpleAsyncTaskExecutor taskExecutor = new SimpleAsyncTaskExecutor("hybrid-partition-");
        taskExecutor.setConcurrencyLimit(partitionCount);
        partitionHandler.setTaskExecutor(taskExecutor);

        try {
            partitionHandler.afterPropertiesSet();
        } catch (Exception e) {
            log.error("Failed to initialize custom hybrid partition handler for step: {}", stepName, e);
        }

        return new StepBuilder(stepName, jobRepository)
                .partitioner("workerStep", customPartitioner)
                .partitionHandler(partitionHandler)
                .build();
    }

    private Partitioner createClusterAwarePartitioner() {
        return new Partitioner() {
            @Override
            public Map<String, ExecutionContext> partition(int gridSize) {
                Map<String, ExecutionContext> partitions = new ConcurrentHashMap<>();
                List<String> aliveNodes = getWorkerNodes();
                String selfId = nodeIdentity.getNodeId();

                if (aliveNodes.size() <= 1) {
                    return createLocalPartitions(gridSize);
                }

                int partitionIndex = 0;
                for (int i = 0; i < gridSize; i++) {
                    String assignedNode = aliveNodes.get(i % aliveNodes.size());
                    boolean isLocal = assignedNode.equals(selfId);

                    ExecutionContext context = new ExecutionContext();
                    context.put("partitionId", i);
                    context.put("nodeId", assignedNode);
                    context.put("isLocal", isLocal);
                    context.put("chunkSize", chunkSize);
                    context.put("startIndex", partitionIndex * chunkSize);
                    context.put("endIndex", (partitionIndex + 1) * chunkSize - 1);
                    context.put("timestamp", System.currentTimeMillis());

                    if (kafkaEnabled) {
                        context.put("kafkaTopic", "batch-partition-" + i);
                        context.put("kafkaPartition", i % 3);
                    }

                    partitions.put("partition-" + i, context);
                    partitionIndex++;
                }

                return partitions;
            }

            private Map<String, ExecutionContext> createLocalPartitions(int gridSize) {
                Map<String, ExecutionContext> partitions = new ConcurrentHashMap<>();
                String selfId = nodeIdentity.getNodeId();

                for (int i = 0; i < gridSize; i++) {
                    ExecutionContext context = putValue(selfId, i, chunkSize);
                    if (kafkaEnabled) {
                        context.put("kafkaTopic", "batch-partition-" + i);
                        context.put("kafkaPartition", i % 3);
                    }
                    partitions.put("partition-" + i, context);
                }
                return partitions;
            }
        };
    }

    @Override
    public boolean isMaster() {
        try {
            return roleDetector.isMaster("batch-master");
        } catch (Exception e) {
            log.warn("Failed to check master status, assuming worker: {}", e.getMessage());
            return false;
        }
    }

    @Override
    public List<String> getWorkerNodes() {
        try {
            return nodeCounter.scanNodeKeys()
                    .map(key -> key.substring(key.lastIndexOf(':') + 1))
                    .collect(Collectors.toList());
        } catch (Exception e) {
            log.warn("Failed to get worker nodes, returning self only: {}", e.getMessage());
            return List.of(nodeIdentity.getNodeId());
        }
    }

    @Override
    public int getPartitionCount() {
        List<String> nodes = getWorkerNodes();
        int nodeCount = Math.max(1, nodes.size());
        return Math.max(partitionCount, nodeCount * 2);
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