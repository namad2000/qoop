package io.qoop.cluster;

import lombok.RequiredArgsConstructor;
import org.redisson.api.RBucket;
import org.redisson.api.RedissonClient;
import org.redisson.api.options.KeysScanOptions;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.stream.Stream;

/**
 * Distributed registry of alive cluster nodes backed by Redis.
 *
 * <p>Each node is stored as a Redis bucket whose TTL is refreshed
 * periodically via heartbeat. When the TTL expires, the node is
 * considered dead and disappears from the registry.
 */
@Component
@RequiredArgsConstructor
public class ClusterNodeCounter {

    private final RedissonClient redisson;
    private final ClusterKeyBuilder keys;

    /**
     * Registers a node with the given TTL.
     */
    public void registerNode(String nodeId, long ttlSeconds) {
        RBucket<String> bucket = redisson.getBucket(keys.nodeKey(nodeId));
        bucket.set("alive");
        bucket.expire(Duration.ofSeconds(ttlSeconds));
    }

    /**
     * Refreshes the TTL of an already-registered node.
     * If the node has been evicted, it is re-registered automatically.
     */
    public void refreshHeartbeat(String nodeId, long ttlSeconds) {
        RBucket<String> bucket = redisson.getBucket(keys.nodeKey(nodeId));
        if (bucket.isExists()) {
            bucket.expire(Duration.ofSeconds(ttlSeconds));
        } else {
            registerNode(nodeId, ttlSeconds);
        }
    }

    /**
     * Removes a node from the registry. Idempotent.
     */
    public void deregisterNode(String nodeId) {
        redisson.getBucket(keys.nodeKey(nodeId)).delete();
    }

    /**
     * Counts alive nodes excluding the given self node id.
     */
    public long countOtherAliveNodes(String selfNodeId) {
        String selfKey = keys.nodeKey(selfNodeId);
        return scanNodeKeys()
                .filter(key -> !key.equals(selfKey))
                .count();
    }

    /**
     * Counts all alive nodes in the cluster.
     */
    public long countAllAliveNodes() {
        return scanNodeKeys().count();
    }

    /**
     * Checks whether the given node is currently alive.
     */
    public boolean isAlive(String nodeId) {
        return redisson.getBucket(keys.nodeKey(nodeId)).isExists();
    }

    /**
     * Scans node keys using the modern non-deprecated API.
     */
    private Stream<String> scanNodeKeys() {
        KeysScanOptions options = KeysScanOptions.defaults()
                .pattern(keys.nodeKeyScanPattern());
        return redisson.getKeys().getKeysStream(options);
    }
}