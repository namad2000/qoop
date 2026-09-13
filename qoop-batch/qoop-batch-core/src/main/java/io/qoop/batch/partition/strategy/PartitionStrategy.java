package io.qoop.batch.partition.strategy;

import org.springframework.batch.core.partition.Partitioner;
import org.springframework.batch.core.partition.support.TaskExecutorPartitionHandler;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.Step;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.infrastructure.item.ExecutionContext;
import org.springframework.core.task.SimpleAsyncTaskExecutor;
import org.springframework.transaction.PlatformTransactionManager;

import java.util.List;

/**
 * Strategy interface for partition processing.
 */
public interface PartitionStrategy {

    Step createPartitionStep(
            String stepName,
            JobRepository jobRepository,
            PlatformTransactionManager transactionManager,
            Step workerStep
    );

    default Step createPartitionStepWithCustomPartitioner(
            String stepName,
            JobRepository jobRepository,
            PlatformTransactionManager transactionManager,
            Step workerStep,
            Partitioner customPartitioner) {

        int gridSize = getPartitionCount();

        TaskExecutorPartitionHandler partitionHandler = new TaskExecutorPartitionHandler();
        partitionHandler.setStep(workerStep);
        partitionHandler.setGridSize(gridSize);

        SimpleAsyncTaskExecutor taskExecutor = new SimpleAsyncTaskExecutor("custom-partition-");
        taskExecutor.setConcurrencyLimit(gridSize);
        partitionHandler.setTaskExecutor(taskExecutor);

        try {
            partitionHandler.afterPropertiesSet();
        } catch (Exception e) {
            throw new IllegalStateException("Failed to initialize TaskExecutorPartitionHandler for custom partitioner", e);
        }

        return new StepBuilder(stepName, jobRepository)
                .partitioner("workerStep", customPartitioner)
                .partitionHandler(partitionHandler)
                .build();
    }

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