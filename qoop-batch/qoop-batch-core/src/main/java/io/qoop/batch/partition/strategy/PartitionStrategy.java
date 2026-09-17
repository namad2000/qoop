package io.qoop.batch.partition.strategy;

import org.springframework.batch.core.partition.Partitioner;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.Step;
import org.springframework.batch.infrastructure.item.ExecutionContext;
import org.springframework.transaction.PlatformTransactionManager;

import java.util.List;

/**
 * Strategy interface for partition processing.
 */
public interface PartitionStrategy {

    /**
     * Every strategy must provide its default Partitioner implementation.
     */
    Partitioner createDefaultPartitioner();

    /**
     * Default implementation using strategy's default partitioner.
     */
    default Step createPartitionStep(
            String stepName,
            JobRepository jobRepository,
            PlatformTransactionManager transactionManager,
            Step workerStep) {
        return createPartitionStep(stepName, jobRepository, transactionManager, workerStep, createDefaultPartitioner());
    }

    /**
     * Creates a partitioned Step using either a custom partitioner or the fallback.
     */
    default Step createPartitionStep(
            String stepName,
            JobRepository jobRepository,
            PlatformTransactionManager transactionManager,
            Step workerStep,
            Partitioner customPartitioner) {

        Partitioner partitionerToUse = (customPartitioner != null)
                ? customPartitioner
                : createDefaultPartitioner();

        return createPartitionStepWithPartitioner(stepName, jobRepository, transactionManager, workerStep, partitionerToUse);
    }

    /**
     * Strategy-specific execution builder that binds the resolved Partitioner into Step infrastructure.
     */
    Step createPartitionStepWithPartitioner(
            String stepName,
            JobRepository jobRepository,
            PlatformTransactionManager transactionManager,
            Step workerStep,
            Partitioner partitioner
    );

    boolean isMaster();

    List<String> getWorkerNodes();

    int getPartitionCount();

    int getChunkSize();

    boolean isKafkaEnabled();

    default ExecutionContext putValue(String selfId, int index, int chunkSize) {
        ExecutionContext context = new ExecutionContext();
        context.put("partitionIndex", index);
        context.put("gridSize", getPartitionCount());
        context.put("nodeId", selfId);
        context.put("isLocal", true);
        context.put("chunkSize", chunkSize);
        context.put("startIndex", index * chunkSize);
        context.put("endIndex", (index + 1) * chunkSize - 1);
        context.put("timestamp", System.currentTimeMillis());
        return context;
    }
}