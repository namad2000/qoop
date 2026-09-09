package io.qoop.batch.config;

import io.qoop.batch.partition.strategy.HybridPartitionStrategy;
import io.qoop.batch.partition.strategy.LocalPartitionStrategy;
import io.qoop.batch.partition.strategy.PartitionStrategy;
import io.qoop.cluster.ClusterNodeCounter;
import io.qoop.cluster.ClusterRoleDetector;
import io.qoop.cluster.NodeIdentity;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

/**
 * Configuration for partition strategy selection.
 *
 * <p>Strategy selection logic:
 * <ol>
 *   <li>If {@code partition.strategy.force-local=true} → LOCAL</li>
 *   <li>If only one node alive → LOCAL</li>
 *   <li>If multiple nodes alive → HYBRID</li>
 * </ol>
 *
 * <p>In both LOCAL and HYBRID modes, each node uses local partitioning
 * with chunk-oriented execution for its assigned partitions.
 *
 * <p>Users can provide custom partitioners at runtime by using the
 * {@link PartitionStrategy#createPartitionStepWithCustomPartitioner} method.
 */
@Slf4j
@Configuration
@RequiredArgsConstructor
public class PartitionConfiguration {

    private final NodeIdentity nodeIdentity;
    private final ClusterNodeCounter nodeCounter;
    private final ClusterRoleDetector roleDetector;

    @Value("${partition.strategy.force-local:false}")
    private boolean forceLocal;

    @Value("${partition.grid-size:4}")
    private int gridSize;

    @Value("${partition.chunk-size:10}")
    private int chunkSize;

    @Value("${kafka.enabled:true}")
    private boolean kafkaEnabled;

    /**
     * Creates the appropriate partition strategy based on cluster state.
     *
     * @return Selected PartitionStrategy instance
     */
    @Bean
    @Primary
    public PartitionStrategy partitionStrategy() {
        int aliveNodes = getAliveNodeCount();

        log.info("=== Partition Strategy Selection ===");
        log.info("Force local: {}", forceLocal);
        log.info("Kafka enabled: {}", kafkaEnabled);
        log.info("Alive nodes: {}", aliveNodes);
        log.info("Grid size: {}", gridSize);
        log.info("Chunk size: {}", chunkSize);

        if (forceLocal || aliveNodes <= 1) {
            String reason = forceLocal ? "forced by configuration" : "only one node alive";
            log.info("Selected: LOCAL strategy ({})", reason);
            log.info("All {} partitions will run locally on node: {}", gridSize, nodeIdentity.getNodeId());
            return new LocalPartitionStrategy(nodeIdentity, gridSize, chunkSize, kafkaEnabled);
        }

        log.info("Selected: HYBRID strategy ({} nodes alive)", aliveNodes);
        log.info("{} partitions will be distributed across {} nodes", gridSize, aliveNodes);
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
     * Counts alive nodes in the cluster with error handling.
     * If Redis is unavailable, falls back to 1 node (local mode).
     *
     * @return Number of alive nodes, minimum 1
     */
    private int getAliveNodeCount() {
        try {
            return (int) nodeCounter.scanNodeKeys().count();
        } catch (Exception e) {
            log.warn("Failed to count alive nodes, falling back to 1: {}", e.getMessage());
            return 1;
        }
    }
}