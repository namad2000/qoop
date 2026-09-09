package io.qoop.batch.partition.strategy;

import io.qoop.cluster.NodeIdentity;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.batch.core.partition.Partitioner;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.Step;
import org.springframework.batch.infrastructure.item.ExecutionContext;
import org.springframework.transaction.PlatformTransactionManager;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.mock;

@ExtendWith(MockitoExtension.class)
@DisplayName("Local Partition Strategy Tests")
class LocalPartitionStrategyTest {

    @Mock
    private NodeIdentity nodeIdentity;

    @Mock
    private JobRepository jobRepository;

    @Mock
    private PlatformTransactionManager transactionManager;

    @Mock
    private Step workerStep;

    private LocalPartitionStrategy strategy;

    @BeforeEach
    void setUp() {
        lenient().when(nodeIdentity.getNodeId()).thenReturn("local-node-1");
        strategy = new LocalPartitionStrategy(nodeIdentity, 4, 10, true);
    }

    @Test
    @DisplayName("Should create partitioned step with local partitioner")
    void shouldCreatePartitionedStepWithLocalPartitioner() {
        // Given
        String stepName = "local-step";

        // When
        Step result = strategy.createPartitionStep(
                stepName,
                jobRepository,
                transactionManager,
                workerStep
        );

        // Then
        assertNotNull(result);
        assertThat(result.getName()).isEqualTo(stepName);
    }

    @Test
    @DisplayName("Should always return true for isMaster")
    void shouldAlwaysReturnTrueForIsMaster() {
        assertThat(strategy.isMaster()).isTrue();
        assertThat(strategy.isMaster()).isTrue();
    }

    @Test
    @DisplayName("Should return only itself as worker nodes")
    void shouldReturnOnlySelfAsWorkerNodes() {
        List<String> workerNodes = strategy.getWorkerNodes();
        assertThat(workerNodes).hasSize(1);
        assertThat(workerNodes).containsExactly("local-node-1");
    }

    @Test
    @DisplayName("Should return configured partition count")
    void shouldReturnConfiguredPartitionCount() {
        assertThat(strategy.getPartitionCount()).isEqualTo(4);
    }

    @Test
    @DisplayName("Should return configured chunk size")
    void shouldReturnConfiguredChunkSize() {
        assertThat(strategy.getChunkSize()).isEqualTo(10);
    }

    @Test
    @DisplayName("Should return kafka enabled flag")
    void shouldReturnKafkaEnabledFlag() {
        assertThat(strategy.isKafkaEnabled()).isTrue();
    }

    @Test
    @DisplayName("Should create step with custom partitioner")
    void shouldCreateStepWithCustomPartitioner() {
        // Given
        String stepName = "custom-local-step";
        Partitioner customPartitioner = mock(Partitioner.class);

        // When
        Step result = strategy.createPartitionStepWithCustomPartitioner(
                stepName,
                jobRepository,
                transactionManager,
                workerStep,
                customPartitioner
        );

        // Then
        assertNotNull(result);
        assertThat(result.getName()).isEqualTo(stepName);
    }

    @Test
    @DisplayName("Should handle different configurations")
    void shouldHandleDifferentConfigurations() {
        // Given
        LocalPartitionStrategy customStrategy = new LocalPartitionStrategy(
                nodeIdentity, 8, 25, false
        );

        // Then
        assertThat(customStrategy.getPartitionCount()).isEqualTo(8);
        assertThat(customStrategy.getChunkSize()).isEqualTo(25);
        assertThat(customStrategy.isKafkaEnabled()).isFalse();
    }

    @Test
    @DisplayName("Should create correct ExecutionContext for each partition")
    void shouldCreateCorrectExecutionContextForEachPartition() {
        // Given
        String selfId = "test-node";
        int chunkSize = 10;

        // When
        ExecutionContext context = strategy.putValue(selfId, 2, chunkSize);

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

        assertThat(partitionId).isEqualTo(2);
        assertThat(nodeId).isEqualTo(selfId);
        assertThat(isLocal).isEqualTo(true);
        assertThat(contextChunkSize).isEqualTo(chunkSize);
        assertThat(startIndex).isEqualTo(20);
        assertThat(endIndex).isEqualTo(29);
        assertThat(timestamp).isNotNull();
    }
}