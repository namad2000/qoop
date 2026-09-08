package io.qoop.cluster;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class NodeLifecycleTest {

    @Mock NodeIdentity nodeIdentity;
    @Mock ClusterNodeCounter nodeCounter;

    private NodeLifecycle lifecycle;

    private static final String NODE_ID = "test-app@host-abc12345";
    private static final long TTL_SECONDS = 60L;

    @BeforeEach
    void setUp() {
        lifecycle = new NodeLifecycle(nodeIdentity, nodeCounter);
        ReflectionTestUtils.setField(lifecycle, "ttlSeconds", TTL_SECONDS);
        when(nodeIdentity.getNodeId()).thenReturn(NODE_ID);
    }

    @Test
    void shouldRegisterNodeOnStart() {
        lifecycle.onStart();

        verify(nodeCounter).registerNode(NODE_ID, TTL_SECONDS);
    }

    @Test
    void shouldSendHeartbeatWithCurrentTtl() {
        lifecycle.refreshHeartbeat();

        verify(nodeCounter).refreshHeartbeat(NODE_ID, TTL_SECONDS);
    }

    @Test
    void shouldDeregisterNodeOnShutdown() {
        lifecycle.onShutdown();

        verify(nodeCounter).deregisterNode(NODE_ID);
    }

    @Test
    void shouldNotThrowWhenHeartbeatFails() {
        doThrow(new RuntimeException("Redis connection lost"))
                .when(nodeCounter).refreshHeartbeat(anyString(), anyLong());

        assertThatCode(() -> lifecycle.refreshHeartbeat())
                .doesNotThrowAnyException();
    }

    @Test
    void shouldNotThrowWhenDeregisterFails() {
        doThrow(new RuntimeException("Redis connection lost"))
                .when(nodeCounter).deregisterNode(anyString());

        assertThatCode(() -> lifecycle.onShutdown())
                .doesNotThrowAnyException();
    }

    @Test
    void shouldUseConfiguredTtlValue() {
        ReflectionTestUtils.setField(lifecycle, "ttlSeconds", 120L);

        lifecycle.refreshHeartbeat();

        verify(nodeCounter).refreshHeartbeat(NODE_ID, 120L);
    }
}