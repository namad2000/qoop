package io.qoop.batch.partition.factory;

import io.qoop.batch.config.BatchKafkaProperties;
import io.qoop.batch.partition.strategy.HybridPartitionStrategy;
import io.qoop.batch.partition.strategy.LocalPartitionStrategy;
import io.qoop.batch.partition.strategy.PartitionStrategy;
import io.qoop.cluster.ClusterNodeCounter;
import io.qoop.cluster.ClusterRoleDetector;
import io.qoop.cluster.NodeIdentity;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.PollableChannel;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Component
public class PartitionStrategyFactory {

    private final NodeIdentity nodeIdentity;
    private final ClusterNodeCounter nodeCounter;
    private final ClusterRoleDetector roleDetector;
    private final BatchKafkaProperties batchKafkaProperties;

    private final MessageChannel outboundRequests;
    private final PollableChannel inboundReplies;

    public PartitionStrategyFactory(
            NodeIdentity nodeIdentity,
            ClusterNodeCounter nodeCounter,
            ClusterRoleDetector roleDetector,
            BatchKafkaProperties batchKafkaProperties,
            @Qualifier("${batch.kafka.namespace:default}-outboundRequests") MessageChannel outboundRequests,
            @Qualifier("${batch.kafka.namespace:default}-inboundReplies") PollableChannel inboundReplies) {
        this.nodeIdentity = nodeIdentity;
        this.nodeCounter = nodeCounter;
        this.roleDetector = roleDetector;
        this.batchKafkaProperties = batchKafkaProperties;
        this.outboundRequests = outboundRequests;
        this.inboundReplies = inboundReplies;
    }

    public PartitionStrategy createStrategy() {
        int aliveNodes = getAliveNodeCount();

        boolean effectiveForceLocal = Boolean.TRUE.equals(batchKafkaProperties.getPartition().getStrategy().getForceLocal());
        int effectiveGridSize = batchKafkaProperties.getPartition().getGridSize() != null ? batchKafkaProperties.getPartition().getGridSize() : 4;
        int effectiveChunkSize = batchKafkaProperties.getPartition().getChunkSize() != null ? batchKafkaProperties.getPartition().getChunkSize() : 10;
        boolean effectiveKafkaEnabled = Boolean.TRUE.equals(batchKafkaProperties.getKafka().getEnabled());

        log.info("=== Runtime Partition Strategy Decision ===");
        log.info("Batch Namespace: {}", batchKafkaProperties.getKafka().getNamespace());
        log.info("Force local config: {}", effectiveForceLocal);
        log.info("Kafka enabled: {}", effectiveKafkaEnabled);
        log.info("Current alive nodes in cluster: {}", aliveNodes);
        log.info("Target Grid size: {}", effectiveGridSize);

        if (effectiveForceLocal || aliveNodes <= 1) {
            String reason = effectiveForceLocal ? "forced by configuration" : "only 1 node alive in cluster";
            log.info("Selected Strategy: LOCAL ({})", reason);
            return new LocalPartitionStrategy(
                    nodeIdentity,
                    effectiveGridSize,
                    effectiveChunkSize,
                    effectiveKafkaEnabled
            );
        }

        log.info("Selected Strategy: HYBRID ({} nodes active)", aliveNodes);
        return new HybridPartitionStrategy(
                nodeIdentity,
                nodeCounter,
                roleDetector,
                batchKafkaProperties,
                effectiveGridSize,
                effectiveChunkSize,
                effectiveKafkaEnabled,
                outboundRequests,
                inboundReplies
        );
    }

    private int getAliveNodeCount() {
        try {
            return (int) nodeCounter.scanNodeKeys().count();
        } catch (Exception e) {
            log.warn("Failed to query alive nodes count from cluster, falling back to 1 (LOCAL): {}", e.getMessage());
            return 1;
        }
    }

    public ClusterState getClusterState() {
        List<String> nodes = getWorkerNodes();
        return new ClusterState(nodes.size(), nodes);
    }

    private List<String> getWorkerNodes() {
        try {
            return nodeCounter.scanNodeKeys()
                    .map(key -> key.substring(key.lastIndexOf(':') + 1))
                    .collect(Collectors.toList());
        } catch (Exception e) {
            log.warn("Failed to fetch worker nodes list, returning self only: {}", e.getMessage());
            return List.of(nodeIdentity.getNodeId());
        }
    }

    public record ClusterState(int nodeCount, List<String> nodeIds) {
    }
}