package io.qoop.batch.partition.factory;

import io.qoop.batch.config.BatchKafkaProperties;
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
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.PollableChannel;

import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("Partition Strategy Factory Unit Tests")
class PartitionStrategyFactoryTest {

    @Mock
    private NodeIdentity nodeIdentity;

    @Mock
    private ClusterNodeCounter nodeCounter;

    @Mock
    private ClusterRoleDetector roleDetector;

    @Mock
    private MessageChannel outboundRequests;

    @Mock
    private PollableChannel inboundReplies;

    private BatchKafkaProperties batchKafkaProperties;
    private PartitionStrategyFactory factory;

    @BeforeEach
    void setUp() {
        lenient().when(nodeIdentity.getNodeId()).thenReturn("test-node-1");

        batchKafkaProperties = new BatchKafkaProperties();
        batchKafkaProperties.getKafka().setNamespace("default");
        batchKafkaProperties.getKafka().setEnabled(true);
        batchKafkaProperties.getPartition().setGridSize(4);
        batchKafkaProperties.getPartition().setChunkSize(10);
        batchKafkaProperties.getPartition().getStrategy().setForceLocal(false);

        factory = new PartitionStrategyFactory(
                nodeIdentity,
                nodeCounter,
                roleDetector,
                batchKafkaProperties,
                outboundRequests,
                inboundReplies
        );
    }

    @Test
    @DisplayName("Should create Local strategy when only one node alive")
    void shouldCreateLocalStrategyWhenSingleNode() {
        when(nodeCounter.scanNodeKeys()).thenReturn(
                Stream.of("qoop:app:cluster:node:test-node-1")
        );

        PartitionStrategy strategy = factory.createStrategy();

        assertNotNull(strategy);
        assertThat(strategy).isInstanceOf(LocalPartitionStrategy.class);
        assertThat(strategy.getPartitionCount()).isEqualTo(4);
        assertThat(strategy.getChunkSize()).isEqualTo(10);
        assertThat(strategy.isMaster()).isTrue();
    }

    @Test
    @DisplayName("Should create Hybrid strategy when multiple nodes alive")
    void shouldCreateHybridStrategyWhenMultipleNodes() {
        when(nodeCounter.scanNodeKeys()).thenReturn(
                Stream.of("node-1", "node-2", "node-3")
                        .map(n -> "qoop:app:cluster:node:" + n)
        );

        PartitionStrategy strategy = factory.createStrategy();

        assertNotNull(strategy);
        assertThat(strategy).isInstanceOf(HybridPartitionStrategy.class);
        assertThat(strategy.getPartitionCount()).isEqualTo(4);
        assertThat(strategy.getChunkSize()).isEqualTo(10);
    }

    @Test
    @DisplayName("Should handle node counting failure gracefully and return Local")
    void shouldHandleNodeCountingFailureGracefully() {
        when(nodeCounter.scanNodeKeys()).thenThrow(new RuntimeException("Redis connection failed"));

        PartitionStrategy strategy = factory.createStrategy();

        assertNotNull(strategy);
        assertThat(strategy).isInstanceOf(LocalPartitionStrategy.class);
    }

    @Test
    @DisplayName("Should create strategy with custom configuration")
    void shouldCreateStrategyWithCustomConfig() {
        batchKafkaProperties.getPartition().setGridSize(8);
        batchKafkaProperties.getPartition().setChunkSize(20);
        batchKafkaProperties.getKafka().setEnabled(false);

        when(nodeCounter.scanNodeKeys()).thenReturn(
                Stream.of("node-1", "node-2")
                        .map(n -> "qoop:app:cluster:node:" + n)
        );

        PartitionStrategy strategy = factory.createStrategy();

        assertNotNull(strategy);
        assertThat(strategy).isInstanceOf(HybridPartitionStrategy.class);
        assertThat(strategy.getPartitionCount()).isEqualTo(8);
        assertThat(strategy.getChunkSize()).isEqualTo(20);
        assertThat(strategy.isKafkaEnabled()).isFalse();
    }

    @Test
    @DisplayName("Should return cluster state with node count and IDs")
    void shouldReturnClusterState() {
        when(nodeCounter.scanNodeKeys()).thenReturn(
                Stream.of("node-1", "node-2", "node-3")
                        .map(n -> "qoop:app:cluster:node:" + n)
        );

        PartitionStrategyFactory.ClusterState state = factory.getClusterState();

        assertNotNull(state);
        assertThat(state.nodeCount()).isEqualTo(3);
        assertThat(state.nodeIds()).containsExactly("node-1", "node-2", "node-3");
    }
}