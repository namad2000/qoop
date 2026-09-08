package io.qoop.cluster;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class NodeLifecycle {

    private final NodeIdentity nodeIdentity;
    private final ClusterNodeCounter nodeCounter;

    @Value("${cluster.heartbeat.ttl-seconds:60}")
    private long ttlSeconds;

    @PostConstruct
    public void onStart() {
        nodeCounter.registerNode(nodeIdentity.getNodeId(), ttlSeconds);
        log.info("Node joined cluster: {}", nodeIdentity.getNodeId());
    }

    @Scheduled(fixedDelayString = "${cluster.heartbeat.interval-seconds:20}",
            initialDelayString = "${cluster.heartbeat.interval-seconds:20}")
    public void refreshHeartbeat() {
        try {
            nodeCounter.refreshHeartbeat(nodeIdentity.getNodeId(), ttlSeconds);
            log.debug("Heartbeat sent for node: {}", nodeIdentity.getNodeId());
        } catch (Exception e) {
            log.warn("Heartbeat failed for node {}: {}",
                    nodeIdentity.getNodeId(), e.getMessage());
        }
    }

    @PreDestroy
    public void onShutdown() {
        try {
            nodeCounter.deregisterNode(nodeIdentity.getNodeId());
            log.info("Node left cluster: {}", nodeIdentity.getNodeId());
        } catch (Exception e) {
            log.warn("Deregister failed for node {}: {}",
                    nodeIdentity.getNodeId(), e.getMessage());
        }
    }
}