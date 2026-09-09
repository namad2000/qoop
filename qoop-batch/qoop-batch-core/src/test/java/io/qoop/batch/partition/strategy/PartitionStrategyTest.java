package io.qoop.batch.partition.strategy;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.batch.core.partition.Partitioner;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.Step;
import org.springframework.batch.infrastructure.item.ExecutionContext;
import org.springframework.transaction.PlatformTransactionManager;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.mock;

@DisplayName("Partition Strategy Interface Tests")
class PartitionStrategyTest {

    @Test
    @DisplayName("Default createPartitionStepWithCustomPartitioner should work")
    void defaultCreatePartitionStepWithCustomPartitionerShouldWork() {
        // Given
        TestPartitionStrategy strategy = new TestPartitionStrategy();
        JobRepository jobRepository = mock(JobRepository.class);
        PlatformTransactionManager transactionManager = mock(PlatformTransactionManager.class);
        Step workerStep = mock(Step.class);
        Partitioner customPartitioner = mock(Partitioner.class);

        // When
        Step result = strategy.createPartitionStepWithCustomPartitioner(
                "test-step",
                jobRepository,
                transactionManager,
                workerStep,
                customPartitioner
        );

        // Then
        assertNotNull(result);
        assertThat(result.getName()).isEqualTo("test-step");
    }

    @Test
    @DisplayName("putValue should create correct ExecutionContext")
    void putValueShouldCreateCorrectExecutionContext() {
        // Given
        TestPartitionStrategy strategy = new TestPartitionStrategy();
        String selfId = "node-1";
        int index = 3;
        int chunkSize = 10;

        // When
        ExecutionContext context = strategy.putValue(selfId, index, chunkSize);

        // Then
        assertNotNull(context);

        Object partitionId = context.get("partitionId");
        Object nodeId = context.get("nodeId");
        Object isLocal = context.get("isLocal");
        Object contextChunkSize = context.get("chunkSize");
        Object startIndex = context.get("startIndex");
        Object endIndex = context.get("endIndex");
        Object timestamp = context.get("timestamp");

        assertNotNull(partitionId);
        assertNotNull(nodeId);
        assertNotNull(isLocal);
        assertNotNull(contextChunkSize);
        assertNotNull(startIndex);
        assertNotNull(endIndex);
        assertNotNull(timestamp);

        assertThat(partitionId).isEqualTo(3);
        assertThat(nodeId).isEqualTo("node-1");
        assertThat(isLocal).isEqualTo(true);
        assertThat(contextChunkSize).isEqualTo(10);
        assertThat(startIndex).isEqualTo(30);
        assertThat(endIndex).isEqualTo(39);
        assertThat(timestamp).isNotNull();
    }

    private static class TestPartitionStrategy implements PartitionStrategy {
        @Override
        public Step createPartitionStep(String stepName, JobRepository jobRepository,
                                        PlatformTransactionManager transactionManager,
                                        Step workerStep) {
            return mock(Step.class);
        }

        @Override
        public boolean isMaster() {
            return true;
        }

        @Override
        public List<String> getWorkerNodes() {
            return List.of("node-1");
        }

        @Override
        public int getPartitionCount() {
            return 4;
        }

        @Override
        public int getChunkSize() {
            return 10;
        }

        @Override
        public boolean isKafkaEnabled() {
            return true;
        }
    }
}