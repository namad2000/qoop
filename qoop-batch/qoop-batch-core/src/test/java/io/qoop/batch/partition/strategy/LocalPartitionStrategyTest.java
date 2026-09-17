package io.qoop.batch.partition.strategy;

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
import org.springframework.transaction.PlatformTransactionManager;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
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
        when(nodeIdentity.getNodeId()).thenReturn("node-1");
        strategy = new LocalPartitionStrategy(nodeIdentity, 4, 100, false);
    }

    @Test
    void testCreateDefaultPartitioner() {
        Partitioner partitioner = strategy.createDefaultPartitioner();
        assertNotNull(partitioner);

        Map<String, ExecutionContext> partitions = partitioner.partition(4);
        assertEquals(4, partitions.size());
        assertTrue(partitions.containsKey("partition-0"));
        assertEquals("node-1", partitions.get("partition-0").get("nodeId"));
    }

    @Test
    void testCreatePartitionStepWithDefaultPartitioner() {
        Step partitionStep = strategy.createPartitionStep("localStep", jobRepository, transactionManager, workerStep);
        assertNotNull(partitionStep);
        assertEquals("localStep", partitionStep.getName());
    }

    @Test
    void testCreatePartitionStepWithCustomPartitioner() {
        Partitioner customPartitioner = gridSize -> Map.of("custom-0", new ExecutionContext());

        Step partitionStep = strategy.createPartitionStep("localStep", jobRepository, transactionManager, workerStep, customPartitioner);
        assertNotNull(partitionStep);
        assertEquals("localStep", partitionStep.getName());
    }
}