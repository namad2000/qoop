package io.qoop.cluster;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class NodeIdentityTest {

    @Test
    void shouldGenerateNonBlankNodeId() {
        NodeIdentity identity = new NodeIdentity("test-app");

        assertThat(identity.getNodeId()).isNotBlank();
    }

    @Test
    void shouldIncludeAppNameInNodeId() {
        NodeIdentity identity = new NodeIdentity("order-service");

        assertThat(identity.getNodeId()).startsWith("order-service@");
    }

    @Test
    void shouldGenerateUniqueNodeIdsAcrossInstances() {
        NodeIdentity first = new NodeIdentity("app");
        NodeIdentity second = new NodeIdentity("app");

        assertThat(first.getNodeId()).isNotEqualTo(second.getNodeId());
    }

    @Test
    void shouldIncludeDelimiterInNodeId() {
        NodeIdentity identity = new NodeIdentity("app");

        // Format: app@hostname-uuid
        assertThat(identity.getNodeId()).contains("@").contains("-");
    }
}