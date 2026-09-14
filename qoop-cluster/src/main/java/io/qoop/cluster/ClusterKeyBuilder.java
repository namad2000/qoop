package io.qoop.cluster;

import lombok.Getter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * Builds consistent Redis keys for the cluster.
 * All keys are namespaced by application name to avoid collisions
 * when multiple qoop-based applications share the same Redis instance.
 */

@Getter
@Component
public class ClusterKeyBuilder {

    private static final String ROOT = "qoop";
    private static final String CLUSTER_NS = "cluster";
    private static final String NODE_NS = "node";
    private static final String MASTER_LOCK_NS = "master-lock";
    private static final String GENERATION_NS = "generation";

    private final String appName;

    public ClusterKeyBuilder(@Value("${spring.application.name:app}") String appName) {
        this.appName = appName;
    }

    /**
     * Returns the full Redis key for a node bucket.
     * Format: qoop:{appName}:cluster:node:{nodeId}
     */
    public String nodeKey(String nodeId) {
        return ROOT + ":" + appName + ":" + CLUSTER_NS + ":" + NODE_NS + ":" + nodeId;
    }

    /**
     * Returns the key prefix used to scan all nodes.
     * Format: qoop:{appName}:cluster:node:*
     */
    public String nodeKeyScanPattern() {
        return ROOT + ":" + appName + ":" + CLUSTER_NS + ":" + NODE_NS + ":*";
    }

    /**
     * Returns the Redis key for a master lock.
     * Format: qoop:{appName}:cluster:master-lock:{jobName}
     */
    public String masterLockKey(String jobName) {
        return ROOT + ":" + appName + ":" + CLUSTER_NS + ":" + MASTER_LOCK_NS + ":" + jobName;
    }

    /**
     * Returns the Redis key for the generation counter of a master.
     * Format: qoop:{appName}:cluster:generation:{jobName}
     */
    public String generationKey(String jobName) {
        return ROOT + ":" + appName + ":" + CLUSTER_NS + ":" + GENERATION_NS + ":" + jobName;
    }
}