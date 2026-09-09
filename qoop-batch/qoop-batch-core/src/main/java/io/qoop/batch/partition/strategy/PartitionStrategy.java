package io.qoop.batch.partition.strategy;

import org.springframework.batch.core.partition.Partitioner;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.Step;
import org.springframework.batch.infrastructure.item.ExecutionContext;
import org.springframework.transaction.PlatformTransactionManager;

import java.util.List;

/**
 * Strategy interface for partition processing.
 * Defines how work is distributed across cluster nodes:
 * <ul>
 *   <li>LOCAL: All partitions on the same node with local chunk-oriented processing</li>
 *   <li>HYBRID: Partitions distributed across multiple nodes, each with local chunk-oriented processing</li>
 * </ul>
 *
 * <p>Users can provide custom partitioners by using the
 * {@link #createPartitionStepWithCustomPartitioner} method.</p>
 */
public interface PartitionStrategy {

    /**
     * Creates a partition step using the default partitioner of the strategy.
     *
     * @param stepName           Name of the step
     * @param jobRepository      Spring Batch job repository
     * @param transactionManager Transaction manager
     * @param workerStep         The standard chunk-oriented step
     * @return Configured Step instance with partitioning
     */
    Step createPartitionStep(
            String stepName,
            JobRepository jobRepository,
            PlatformTransactionManager transactionManager,
            Step workerStep
    );

    /**
     * Creates a partition step with a custom partitioner provided by the user.
     * This allows users to implement their own partitioning logic.
     *
     * @param stepName           Name of the step
     * @param jobRepository      Spring Batch job repository
     * @param transactionManager Transaction manager
     * @param workerStep         The standard chunk-oriented step
     * @param customPartitioner  User-provided custom partitioner implementation
     * @return Configured Step instance with custom partitioning
     */
    default Step createPartitionStepWithCustomPartitioner(
            String stepName,
            JobRepository jobRepository,
            PlatformTransactionManager transactionManager,
            Step workerStep,
            Partitioner customPartitioner) {

        return new org.springframework.batch.core.step.builder.StepBuilder(stepName, jobRepository)
                .partitioner("workerStep", customPartitioner)
                .step(workerStep)
                .gridSize(getPartitionCount())
                .build();
    }

    /**
     * Checks if this node is the master/coordinator in the cluster.
     *
     * @return true if this node is the master, false otherwise
     */
    boolean isMaster();

    /**
     * Gets all alive worker nodes in the cluster.
     *
     * @return List of node IDs currently active in the cluster
     */
    List<String> getWorkerNodes();

    /**
     * Gets the number of partitions to create for the job.
     *
     * @return partition count
     */
    int getPartitionCount();

    /**
     * Gets the chunk size for each partition.
     *
     * @return chunk size for processing
     */
    int getChunkSize();

    /**
     * Checks if Kafka is enabled for inter-node communication.
     *
     * @return true if Kafka is enabled, false otherwise
     */
    boolean isKafkaEnabled();

    /**
     * Creates an ExecutionContext with default values for a partition.
     * This is a helper method used by both Local and Hybrid strategies.
     *
     * @param selfId    The ID of the current node
     * @param index     The partition index
     * @param chunkSize The chunk size for processing
     * @return Populated ExecutionContext instance
     */
    default ExecutionContext putValue(String selfId, int index, int chunkSize) {
        ExecutionContext context = new ExecutionContext();
        context.put("partitionId", index);
        context.put("nodeId", selfId);
        context.put("isLocal", true);
        context.put("chunkSize", chunkSize);
        context.put("startIndex", index * chunkSize);
        context.put("endIndex", (index + 1) * chunkSize - 1);
        context.put("timestamp", System.currentTimeMillis());
        return context;
    }
}