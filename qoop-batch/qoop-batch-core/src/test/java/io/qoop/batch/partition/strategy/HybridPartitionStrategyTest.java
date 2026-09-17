package io.qoop.batch.partition.strategy;

import io.qoop.batch.config.BatchKafkaProperties;
import io.qoop.cluster.ClusterNodeCounter;
import io.qoop.cluster.ClusterRoleDetector;
import io.qoop.cluster.NodeIdentity;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.batch.core.partition.Partitioner;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.Step;
import org.springframework.batch.infrastructure.item.ExecutionContext;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.PollableChannel;
import org.springframework.transaction.PlatformTransactionManager;

import java.util.Map;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.anyString;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class HybridPartitionStrategyTest {

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
    @Mock
    private JobRepository jobRepository;
    @Mock
    private PlatformTransactionManager transactionManager;
    @Mock
    private Step workerStep;

    private BatchKafkaProperties batchKafkaProperties;
    private HybridPartitionStrategy strategy;

    @BeforeEach
    void setUp() {
        when(nodeIdentity.getNodeId()).thenReturn("node-master");
        when(workerStep.getName()).thenReturn("workerStep");

        batchKafkaProperties = new BatchKafkaProperties();
        batchKafkaProperties.getKafka().setNamespace("default");

        strategy = new HybridPartitionStrategy(
                nodeIdentity, nodeCounter, roleDetector, batchKafkaProperties, 4, 50, true,
                outboundRequests, inboundReplies
        );
    }

    @Test
    void testCreateDefaultPartitionerClusterAware() {
        when(nodeCounter.scanNodeKeys()).thenReturn(Stream.of("node:node-master", "node:node-worker-1"));

        Partitioner partitioner = strategy.createDefaultPartitioner();
        assertNotNull(partitioner);

        Map<String, ExecutionContext> partitions = partitioner.partition(4);
        assertEquals(4, partitions.size());
        assertEquals("node:node-master", partitions.get("partition-0").get("nodeId"));
        assertEquals("node:node-worker-1", partitions.get("partition-1").get("nodeId"));
    }

    @Test
    void testCreatePartitionStepAsMasterWithCustomPartitioner() {
        when(roleDetector.isMaster(anyString())).thenReturn(true);
        when(nodeCounter.scanNodeKeys()).thenReturn(Stream.of("node:node-master"));

        Partitioner customPartitioner = gridSize -> Map.of("custom-hybrid-0", new ExecutionContext());

        Step step = strategy.createPartitionStep("hybridStep", jobRepository, transactionManager, workerStep, customPartitioner);
        assertNotNull(step);
        assertEquals("hybridStep", step.getName());
    }

    @Test
    void testCreatePartitionStepAsWorkerReturnsLocalStep() {
        when(roleDetector.isMaster(anyString())).thenReturn(false);

        Step step = strategy.createPartitionStep("hybridStep", jobRepository, transactionManager, workerStep);
        assertNotNull(step);
        // Returns workerStep directly when acting as worker
        assertEquals("workerStep", step.getName());
    }
}