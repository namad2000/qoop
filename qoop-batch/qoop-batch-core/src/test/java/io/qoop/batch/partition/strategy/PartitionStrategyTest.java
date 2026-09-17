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
import static org.mockito.Mockito.when;

@DisplayName("Partition Strategy Interface Unit Tests")
class PartitionStrategyTest {

    @Test
    @DisplayName("Default createPartitionStep with custom Partitioner should delegate properly")
    void defaultCreatePartitionStepWithCustomPartitionerShouldWork() {
        // Given
        TestPartitionStrategy strategy = new TestPartitionStrategy();
        JobRepository jobRepository = mock(JobRepository.class);
        PlatformTransactionManager transactionManager = mock(PlatformTransactionManager.class);
        Step workerStep = mock(Step.class);
        Partitioner customPartitioner = mock(Partitioner.class);

        // When
        Step result = strategy.createPartitionStep(
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
    @DisplayName("Default createPartitionStep without Partitioner should fallback to default partitioner")
    void defaultCreatePartitionStepWithoutPartitionerShouldWork() {
        // Given
        TestPartitionStrategy strategy = new TestPartitionStrategy();
        JobRepository jobRepository = mock(JobRepository.class);
        PlatformTransactionManager transactionManager = mock(PlatformTransactionManager.class);
        Step workerStep = mock(Step.class);

        // When
        Step result = strategy.createPartitionStep(
                "test-step-no-partitioner",
                jobRepository,
                transactionManager,
                workerStep
        );

        // Then
        assertNotNull(result);
        assertThat(result.getName()).isEqualTo("test-step-no-partitioner");
    }

    @Test
    @DisplayName("putValue should correctly populate ExecutionContext properties")
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

        assertThat(context.get("partitionIndex")).isEqualTo(3);
        assertThat(context.get("gridSize")).isEqualTo(4);
        assertThat(context.get("nodeId")).isEqualTo("node-1");
        assertThat(context.get("isLocal")).isEqualTo(true);
        assertThat(context.get("chunkSize")).isEqualTo(10);
        assertThat(context.get("startIndex")).isEqualTo(30);
        assertThat(context.get("endIndex")).isEqualTo(39);
        assertThat(context.get("timestamp")).isNotNull();
    }

    private static class TestPartitionStrategy implements PartitionStrategy {

        @Override
        public Partitioner createDefaultPartitioner() {
            return mock(Partitioner.class);
        }

        @Override
        public Step createPartitionStepWithPartitioner(
                String stepName,
                JobRepository jobRepository,
                PlatformTransactionManager transactionManager,
                Step workerStep,
                Partitioner partitioner) {
            Step mockStep = mock(Step.class);
            when(mockStep.getName()).thenReturn(stepName);
            return mockStep;
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