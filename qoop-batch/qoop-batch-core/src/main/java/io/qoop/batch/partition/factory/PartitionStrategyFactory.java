package io.qoop.batch.partition.factory;

import io.qoop.batch.partition.strategy.HybridPartitionStrategy;
import io.qoop.batch.partition.strategy.LocalPartitionStrategy;
import io.qoop.batch.partition.strategy.PartitionStrategy;
import io.qoop.cluster.ClusterNodeCounter;
import io.qoop.cluster.ClusterRoleDetector;
import io.qoop.cluster.NodeIdentity;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Factory that creates partition strategies based on current cluster state.
 * Called automatically at runtime for each job execution.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class PartitionStrategyFactory {

    private final NodeIdentity nodeIdentity;
    private final ClusterNodeCounter nodeCounter;
    private final ClusterRoleDetector roleDetector;

    private static final int DEFAULT_GRID_SIZE = 4;
    private static final int DEFAULT_CHUNK_SIZE = 10;
    private static final boolean DEFAULT_KAFKA_ENABLED = true;

    /**
     * Creates strategy based on current cluster state.
     *
     * @return LocalStrategy if single node, HybridStrategy if multiple nodes
     */
    public PartitionStrategy createStrategy() {
        int aliveNodes = getAliveNodeCount();

        log.info("=== Creating Partition Strategy ===");
        log.info("Alive nodes: {}", aliveNodes);

        if (aliveNodes <= 1) {
            log.info("Selected: LOCAL strategy");
            return new LocalPartitionStrategy(
                    nodeIdentity,
                    DEFAULT_GRID_SIZE,
                    DEFAULT_CHUNK_SIZE,
                    DEFAULT_KAFKA_ENABLED
            );
        }

        log.info("Selected: HYBRID strategy ({} nodes)", aliveNodes);
        return new HybridPartitionStrategy(
                nodeIdentity,
                nodeCounter,
                roleDetector,
                DEFAULT_GRID_SIZE,
                DEFAULT_CHUNK_SIZE,
                DEFAULT_KAFKA_ENABLED
        );
    }

    /**
     * Creates strategy with custom configuration.
     */
    public PartitionStrategy createStrategy(int gridSize, int chunkSize, boolean kafkaEnabled) {
        int aliveNodes = getAliveNodeCount();

        if (aliveNodes <= 1) {
            return new LocalPartitionStrategy(nodeIdentity, gridSize, chunkSize, kafkaEnabled);
        }

        return new HybridPartitionStrategy(
                nodeIdentity,
                nodeCounter,
                roleDetector,
                gridSize,
                chunkSize,
                kafkaEnabled
        );
    }

    /**
     * Gets current cluster state information.
     */
    public ClusterState getClusterState() {
        List<String> nodes = getWorkerNodes();
        return new ClusterState(nodes.size(), nodes);
    }

    private int getAliveNodeCount() {
        try {
            return (int) nodeCounter.scanNodeKeys().count();
        } catch (Exception e) {
            log.warn("Failed to count alive nodes, assuming 1: {}", e.getMessage());
            return 1;
        }
    }

    private List<String> getWorkerNodes() {
        try {
            return nodeCounter.scanNodeKeys()
                    .map(key -> key.substring(key.lastIndexOf(':') + 1))
                    .collect(Collectors.toList());
        } catch (Exception e) {
            log.warn("Failed to get worker nodes, returning self only: {}", e.getMessage());
            return List.of(nodeIdentity.getNodeId());
        }
    }

    /**
     * DTO for cluster state.
     */
    public record ClusterState(int nodeCount, List<String> nodeIds) {
    }
}