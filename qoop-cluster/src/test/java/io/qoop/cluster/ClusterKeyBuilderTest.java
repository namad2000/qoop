package io.qoop.cluster;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class ClusterKeyBuilderTest {

    private final ClusterKeyBuilder keys = new ClusterKeyBuilder("order-service");

    @Test
    void shouldBuildNodeKey() {
        String key = keys.nodeKey("node-abc-123");

        assertThat(key).isEqualTo("qoop:order-service:cluster:node:node-abc-123");
    }

    @Test
    void shouldBuildNodeScanPattern() {
        String pattern = keys.nodeKeyScanPattern();

        assertThat(pattern).isEqualTo("qoop:order-service:cluster:node:*");
    }

    @Test
    void shouldBuildMasterLockKey() {
        String key = keys.masterLockKey("ordersIngestJob");

        assertThat(key).isEqualTo("qoop:order-service:cluster:master-lock:ordersIngestJob");
    }

    @Test
    void shouldBuildGenerationKey() {
        String key = keys.generationKey("ordersIngestJob");

        assertThat(key).isEqualTo("qoop:order-service:cluster:generation:ordersIngestJob");
    }

    @Test
    void shouldNamespaceKeysByAppName() {
        ClusterKeyBuilder otherApp = new ClusterKeyBuilder("payment-service");

        assertThat(keys.nodeKey("n1"))
                .isNotEqualTo(otherApp.nodeKey("n1"))
                .startsWith("qoop:order-service:")
                .doesNotStartWith("qoop:payment-service:");
    }

    @Test
    void shouldHandleDefaultAppName() {
        ClusterKeyBuilder defaultKeys = new ClusterKeyBuilder("app");

        assertThat(defaultKeys.nodeKey("n1")).startsWith("qoop:app:");
    }
}