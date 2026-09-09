package io.qoop.batch.partition.strategy;

import io.qoop.cluster.ClusterNodeCounter;
import io.qoop.cluster.ClusterRoleDetector;
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
import java.util.stream.Collectors;

/**
 * Hybrid partitioning strategy for multi-node cluster execution.
 *
 * <p>This strategy distributes partitions across all alive nodes.
 * Each node processes its assigned partitions locally with chunk-oriented execution.
 *
 * <p>Behavior:
 * <ul>
 *   <li><b>Master Node:</b> Creates partitioned step and distributes partitions to all nodes</li>
 *   <li><b>Worker Node:</b> Executes its assigned partitions locally (chunk-oriented)</li>
 * </ul>
 *
 * <p>Each node processes its partitions independently using local partitioning.
 * This provides true parallel processing across the cluster.
 *
 * <p>Users can provide custom partitioners by using
 * {@link #createPartitionStepWithCustomPartitioner} method.
 *
 * <p>Uses qoop-cluster for:
 * <ul>
 *   <li>Master election via {@link ClusterRoleDetector}</li>
 *   <li>Node discovery via {@link ClusterNodeCounter}</li>
 * </ul>
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

        log.info("Creating HYBRID partition step: {} with {} partitions",
                stepName, partitionCount);
        log.info("Kafka enabled: {}, Node: {}", kafkaEnabled, nodeIdentity.getNodeId());

        if (!isMaster()) {
            log.info("Worker mode for node: {} - executing local partitions", nodeIdentity.getNodeId());
            return workerStep;
        }

        log.info("Master mode for node: {} - distributing {} partitions",
                nodeIdentity.getNodeId(), partitionCount);

        Partitioner partitioner = createClusterAwarePartitioner();

        return new StepBuilder(stepName, jobRepository)
                .partitioner("workerStep", partitioner)
                .step(workerStep)
                .gridSize(partitionCount)
                .build();
    }

    /**
     * Creates a partition step with a custom partitioner in hybrid mode.
     *
     * <p>Only the master node uses the custom partitioner for distribution.
     * Worker nodes execute their assigned partitions normally.
     *
     * @param stepName           Name of the step
     * @param jobRepository      Spring Batch job repository
     * @param transactionManager Transaction manager
     * @param workerStep         The standard chunk-oriented step
     * @param customPartitioner  User-provided custom partitioner
     * @return Configured Step instance with custom hybrid partitioning
     */
    public Step createPartitionStepWithCustomPartitioner(
            String stepName,
            JobRepository jobRepository,
            PlatformTransactionManager transactionManager,
            Step workerStep,
            Partitioner customPartitioner) {

        log.info("Creating HYBRID partition step with CUSTOM partitioner: {}",
                customPartitioner.getClass().getSimpleName());

        if (!isMaster()) {
            log.info("Worker mode for node: {} - executing local partitions", nodeIdentity.getNodeId());
            return workerStep;
        }

        log.info("Master mode for node: {} - distributing {} partitions with custom partitioner",
                nodeIdentity.getNodeId(), partitionCount);

        return new StepBuilder(stepName, jobRepository)
                .partitioner("workerStep", customPartitioner)
                .step(workerStep)
                .gridSize(partitionCount)
                .build();
    }

    /**
     * Creates the default cluster-aware partitioner.
     * Distributes partitions across all alive nodes using round-robin.
     * Each node processes its partitions locally.
     *
     * @return Default cluster-aware Partitioner instance
     */
    private Partitioner createClusterAwarePartitioner() {
        return new Partitioner() {
            @Override
            public Map<String, ExecutionContext> partition(int gridSize) {
                Map<String, ExecutionContext> partitions = new ConcurrentHashMap<>();

                List<String> aliveNodes = getWorkerNodes();
                String selfId = nodeIdentity.getNodeId();

                log.info("Distributing {} partitions across {} nodes (self: {})", gridSize, aliveNodes.size(), selfId);

                // If only one node exists, assign all partitions locally
                if (aliveNodes.size() <= 1) {
                    log.info("Only one node alive, assigning all {} partitions locally", gridSize);
                    return createLocalPartitions(gridSize);
                }

                // Round-robin distribution across all alive nodes
                int partitionIndex = 0;
                for (int i = 0; i < gridSize; i++) {
                    String assignedNode = aliveNodes.get(i % aliveNodes.size());
                    boolean isLocal = assignedNode.equals(selfId);

                    ExecutionContext context = new ExecutionContext();

                    // Partition metadata for local processing on each node
                    context.put("partitionId", i);
                    context.put("nodeId", assignedNode);
                    context.put("isLocal", isLocal);
                    context.put("chunkSize", chunkSize);
                    context.put("startIndex", partitionIndex * chunkSize);
                    context.put("endIndex", (partitionIndex + 1) * chunkSize - 1);
                    context.put("timestamp", System.currentTimeMillis());

                    // Kafka metadata for inter-node communication
                    if (kafkaEnabled) {
                        context.put("kafkaTopic", "batch-partition-" + i);
                        context.put("kafkaPartition", i % 3);
                    }

                    partitions.put("partition-" + i, context);

                    log.debug("Partition {} assigned to node: {} (local: {})",
                            i, assignedNode, isLocal);
                    partitionIndex++;
                }

                log.info("Created {} partitions across {} nodes", partitions.size(), aliveNodes.size());
                return partitions;
            }

            /**
             * Creates local partitions when only one node is available.
             * All partitions are processed locally on the same node.
             *
             * @param gridSize Number of partitions to create
             * @return Map of partition name to ExecutionContext
             */
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

                log.info("Created {} local partitions", partitions.size());
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
        // Ensure at least 2 partitions per node for balanced distribution
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