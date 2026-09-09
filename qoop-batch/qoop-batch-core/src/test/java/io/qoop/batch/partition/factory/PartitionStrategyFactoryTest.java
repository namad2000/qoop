package io.qoop.batch.partition.factory;

import io.qoop.batch.partition.strategy.HybridPartitionStrategy;
import io.qoop.batch.partition.strategy.LocalPartitionStrategy;
import io.qoop.batch.partition.strategy.PartitionStrategy;
import io.qoop.cluster.ClusterNodeCounter;
import io.qoop.cluster.ClusterRoleDetector;
import io.qoop.cluster.NodeIdentity;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("Partition Strategy Factory Tests")
class PartitionStrategyFactoryTest {

    @Mock
    private NodeIdentity nodeIdentity;

    @Mock
    private ClusterNodeCounter nodeCounter;

    @Mock
    private ClusterRoleDetector roleDetector;

    private PartitionStrategyFactory factory;

    @BeforeEach
    void setUp() {
        lenient().when(nodeIdentity.getNodeId()).thenReturn("test-node-1");
        factory = new PartitionStrategyFactory(nodeIdentity, nodeCounter, roleDetector);
    }

    @Test
    @DisplayName("Should create Local strategy when only one node alive")
    void shouldCreateLocalStrategyWhenSingleNode() {
        // Given
        when(nodeCounter.scanNodeKeys()).thenReturn(
                Stream.of("qoop:app:cluster:node:test-node-1")
        );

        // When
        PartitionStrategy strategy = factory.createStrategy();

        // Then
        assertNotNull(strategy);
        assertThat(strategy).isInstanceOf(LocalPartitionStrategy.class);
        assertThat(strategy.getPartitionCount()).isEqualTo(4);
        assertThat(strategy.getChunkSize()).isEqualTo(10);
        assertThat(strategy.isMaster()).isTrue();
    }

    @Test
    @DisplayName("Should create Hybrid strategy when multiple nodes alive")
    void shouldCreateHybridStrategyWhenMultipleNodes() {
        // Given
        when(nodeCounter.scanNodeKeys()).thenReturn(
                Stream.of("node-1", "node-2", "node-3")
                        .map(n -> "qoop:app:cluster:node:" + n)
        );

        // When
        PartitionStrategy strategy = factory.createStrategy();

        // Then
        assertNotNull(strategy);
        assertThat(strategy).isInstanceOf(HybridPartitionStrategy.class);
        assertThat(strategy.getPartitionCount()).isEqualTo(4);
        assertThat(strategy.getChunkSize()).isEqualTo(10);
    }

    @Test
    @DisplayName("Should handle node counting failure gracefully and return Local")
    void shouldHandleNodeCountingFailureGracefully() {
        // Given
        when(nodeCounter.scanNodeKeys()).thenThrow(new RuntimeException("Redis connection failed"));

        // When
        PartitionStrategy strategy = factory.createStrategy();

        // Then
        assertNotNull(strategy);
        assertThat(strategy).isInstanceOf(LocalPartitionStrategy.class);
    }

    @Test
    @DisplayName("Should create strategy with custom configuration")
    void shouldCreateStrategyWithCustomConfig() {
        // Given
        when(nodeCounter.scanNodeKeys()).thenReturn(
                Stream.of("node-1", "node-2")
                        .map(n -> "qoop:app:cluster:node:" + n)
        );

        // When
        PartitionStrategy strategy = factory.createStrategy(8, 20, false);

        // Then
        assertNotNull(strategy);
        assertThat(strategy).isInstanceOf(HybridPartitionStrategy.class);
        assertThat(strategy.getPartitionCount()).isEqualTo(8);
        assertThat(strategy.getChunkSize()).isEqualTo(20);
        assertThat(strategy.isKafkaEnabled()).isFalse();
    }

    @Test
    @DisplayName("Should return Local strategy with custom config when single node")
    void shouldReturnLocalWithCustomConfigWhenSingleNode() {
        // Given
        when(nodeCounter.scanNodeKeys()).thenReturn(
                Stream.of("qoop:app:cluster:node:test-node-1")
        );

        // When
        PartitionStrategy strategy = factory.createStrategy(6, 15, true);

        // Then
        assertNotNull(strategy);
        assertThat(strategy).isInstanceOf(LocalPartitionStrategy.class);
        assertThat(strategy.getPartitionCount()).isEqualTo(6);
        assertThat(strategy.getChunkSize()).isEqualTo(15);
        assertThat(strategy.isKafkaEnabled()).isTrue();
    }

    @Test
    @DisplayName("Should return cluster state with node count and IDs")
    void shouldReturnClusterState() {
        // Given
        when(nodeCounter.scanNodeKeys()).thenReturn(
                Stream.of("node-1", "node-2", "node-3")
                        .map(n -> "qoop:app:cluster:node:" + n)
        );

        // When
        PartitionStrategyFactory.ClusterState state = factory.getClusterState();

        // Then
        assertNotNull(state);
        assertThat(state.nodeCount()).isEqualTo(3);
        assertThat(state.nodeIds()).containsExactly("node-1", "node-2", "node-3");
    }

    @Test
    @DisplayName("Should return cluster state with single node when discovery fails")
    void shouldReturnClusterStateWithSingleNodeWhenDiscoveryFails() {
        // Given
        when(nodeCounter.scanNodeKeys()).thenThrow(new RuntimeException("Redis error"));

        // When
        PartitionStrategyFactory.ClusterState state = factory.getClusterState();

        // Then
        assertNotNull(state);
        assertThat(state.nodeCount()).isEqualTo(1);
        assertThat(state.nodeIds()).containsExactly("test-node-1");
    }
}