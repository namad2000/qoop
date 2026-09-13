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
 */
@Slf4j
@Configuration
@RequiredArgsConstructor
public class PartitionConfiguration {

    private final NodeIdentity nodeIdentity;
    private final ClusterNodeCounter nodeCounter;
    private final ClusterRoleDetector roleDetector;

    @Value("${batch.partition.strategy.force-local:false}")
    private Boolean forceLocal;

    @Value("${batch.partition.grid-size:4}")
    private Integer gridSize;

    @Value("${batch.partition.chunk-size:10}")
    private Integer chunkSize;

    @Value("${batch.kafka.enabled:true}")
    private Boolean kafkaEnabled;

    /**
     * Creates the appropriate partition strategy based on cluster state.
     *
     * @return Selected PartitionStrategy instance
     */
    @Bean
    @Primary
    public PartitionStrategy partitionStrategy() {
        int aliveNodes = getAliveNodeCount();

        boolean effectiveForceLocal = Boolean.TRUE.equals(forceLocal);
        int effectiveGridSize = (gridSize != null) ? gridSize : 4;
        int effectiveChunkSize = (chunkSize != null) ? chunkSize : 10;
        boolean effectiveKafkaEnabled = Boolean.TRUE.equals(kafkaEnabled);

        log.info("=== Partition Strategy Selection ===");
        log.info("Force local: {}", effectiveForceLocal);
        log.info("Kafka enabled: {}", effectiveKafkaEnabled);
        log.info("Alive nodes: {}", aliveNodes);
        log.info("Grid size: {}", effectiveGridSize);
        log.info("Chunk size: {}", effectiveChunkSize);

        if (effectiveForceLocal || aliveNodes <= 1) {
            String reason = effectiveForceLocal ? "forced by configuration" : "only one node alive";
            log.info("Selected: LOCAL strategy ({})", reason);
            log.info("All {} partitions will run locally on node: {}", effectiveGridSize, nodeIdentity.getNodeId());
            return new LocalPartitionStrategy(nodeIdentity, effectiveGridSize, effectiveChunkSize, effectiveKafkaEnabled);
        }

        log.info("Selected: HYBRID strategy ({} nodes alive)", aliveNodes);
        log.info("{} partitions will be distributed across {} nodes", effectiveGridSize, aliveNodes);
        return new HybridPartitionStrategy(
                nodeIdentity,
                nodeCounter,
                roleDetector,
                effectiveGridSize,
                effectiveChunkSize,
                effectiveKafkaEnabled
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