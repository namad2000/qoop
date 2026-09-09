package io.qoop.batch.partition.strategy;

import io.qoop.cluster.ClusterNodeCounter;
import io.qoop.cluster.ClusterRoleDetector;
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
import org.springframework.transaction.PlatformTransactionManager;

import java.util.List;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("Hybrid Partition Strategy Tests")
class HybridPartitionStrategyTest {

    @Mock
    private NodeIdentity nodeIdentity;

    @Mock
    private ClusterNodeCounter nodeCounter;

    @Mock
    private ClusterRoleDetector roleDetector;

    @Mock
    private JobRepository jobRepository;

    @Mock
    private PlatformTransactionManager transactionManager;

    @Mock
    private Step workerStep;

    private HybridPartitionStrategy strategy;

    @BeforeEach
    void setUp() {
        lenient().when(nodeIdentity.getNodeId()).thenReturn("hybrid-node-1");
        lenient().when(workerStep.getName()).thenReturn("mockWorkerStep");
        strategy = new HybridPartitionStrategy(
                nodeIdentity,
                nodeCounter,
                roleDetector,
                4,
                10,
                true
        );
    }

    @Test
    @DisplayName("Should create partitioned step when node is master")
    void shouldCreatePartitionedStepWhenMaster() {
        // Given
        lenient().when(roleDetector.isMaster("batch-master")).thenReturn(true);
        lenient().when(nodeCounter.scanNodeKeys()).thenReturn(
                Stream.of("node-1", "node-2", "node-3")
                        .map(n -> "qoop:app:cluster:node:" + n)
        );

        // When
        Step result = strategy.createPartitionStep(
                "hybrid-step",
                jobRepository,
                transactionManager,
                workerStep
        );

        // Then
        assertNotNull(result);
        assertThat(result.getName()).isEqualTo("hybrid-step");
        verify(roleDetector, atLeastOnce()).isMaster("batch-master");
    }

    @Test
    @DisplayName("Should return worker step when node is not master")
    void shouldReturnWorkerStepWhenNotMaster() {
        // Given
        when(roleDetector.isMaster("batch-master")).thenReturn(false);

        // When
        Step result = strategy.createPartitionStep(
                "worker-step",
                jobRepository,
                transactionManager,
                workerStep
        );

        // Then
        assertThat(result).isSameAs(workerStep);
    }

    @Test
    @DisplayName("Should return true when master")
    void shouldReturnTrueWhenMaster() {
        when(roleDetector.isMaster("batch-master")).thenReturn(true);
        assertThat(strategy.isMaster()).isTrue();
    }

    @Test
    @DisplayName("Should return false when not master")
    void shouldReturnFalseWhenNotMaster() {
        when(roleDetector.isMaster("batch-master")).thenReturn(false);
        assertThat(strategy.isMaster()).isFalse();
    }

    @Test
    @DisplayName("Should handle master check failure gracefully")
    void shouldHandleMasterCheckFailureGracefully() {
        when(roleDetector.isMaster("batch-master")).thenThrow(new RuntimeException("Redis error"));
        assertThat(strategy.isMaster()).isFalse();
    }

    @Test
    @DisplayName("Should return all alive nodes as workers")
    void shouldReturnAllAliveNodesAsWorkers() {
        List<String> expectedNodes = List.of("node-1", "node-2", "node-3");
        when(nodeCounter.scanNodeKeys()).thenReturn(
                expectedNodes.stream().map(n -> "qoop:app:cluster:node:" + n)
        );

        List<String> workerNodes = strategy.getWorkerNodes();
        assertThat(workerNodes).hasSize(3);
        assertThat(workerNodes).containsExactlyElementsOf(expectedNodes);
    }

    @Test
    @DisplayName("Should handle empty worker nodes gracefully")
    void shouldHandleEmptyWorkerNodesGracefully() {
        when(nodeCounter.scanNodeKeys()).thenReturn(Stream.empty());
        List<String> workerNodes = strategy.getWorkerNodes();
        assertThat(workerNodes).isEmpty();
    }

    @Test
    @DisplayName("Should handle worker node discovery failure gracefully")
    void shouldHandleWorkerNodeDiscoveryFailureGracefully() {
        when(nodeCounter.scanNodeKeys()).thenThrow(new RuntimeException("Redis error"));
        List<String> workerNodes = strategy.getWorkerNodes();
        assertThat(workerNodes).hasSize(1);
        assertThat(workerNodes).containsExactly("hybrid-node-1");
    }

    @Test
    @DisplayName("Should calculate partition count based on cluster size")
    void shouldCalculatePartitionCountBasedOnClusterSize() {
        List<String> nodes = List.of("node-1", "node-2", "node-3");
        when(nodeCounter.scanNodeKeys()).thenReturn(
                nodes.stream().map(n -> "qoop:app:cluster:node:" + n)
        );

        int partitionCount = strategy.getPartitionCount();
        assertThat(partitionCount).isEqualTo(6);
    }

    @Test
    @DisplayName("Should maintain minimum 2 partitions per node")
    void shouldMaintainMinimumTwoPartitionsPerNode() {
        List<String> nodes = List.of("node-1", "node-2", "node-3", "node-4", "node-5");
        when(nodeCounter.scanNodeKeys()).thenReturn(
                nodes.stream().map(n -> "qoop:app:cluster:node:" + n)
        );

        int partitionCount = strategy.getPartitionCount();
        assertThat(partitionCount).isEqualTo(10);
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
    @DisplayName("Should create step with custom partitioner when master")
    void shouldCreateStepWithCustomPartitionerWhenMaster() {
        // Given
        lenient().when(roleDetector.isMaster("batch-master")).thenReturn(true);
        lenient().when(nodeCounter.scanNodeKeys()).thenReturn(
                Stream.of("node-1", "node-2")
                        .map(n -> "qoop:app:cluster:node:" + n)
        );

        var customPartitioner = mock(Partitioner.class);

        Step result = strategy.createPartitionStepWithCustomPartitioner(
                "custom-hybrid-step",
                jobRepository,
                transactionManager,
                workerStep,
                customPartitioner
        );

        assertNotNull(result);
        assertThat(result.getName()).isEqualTo("custom-hybrid-step");
    }

    @Test
    @DisplayName("Should return worker step with custom partitioner when not master")
    void shouldReturnWorkerStepWithCustomPartitionerWhenNotMaster() {
        when(roleDetector.isMaster("batch-master")).thenReturn(false);

        var customPartitioner = mock(Partitioner.class);

        Step result = strategy.createPartitionStepWithCustomPartitioner(
                "custom-hybrid-step",
                jobRepository,
                transactionManager,
                workerStep,
                customPartitioner
        );

        assertThat(result).isSameAs(workerStep);
    }
}