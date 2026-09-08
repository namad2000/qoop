package io.qoop.cluster;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.redisson.api.RBucket;
import org.redisson.api.RKeys;
import org.redisson.api.RedissonClient;
import org.redisson.api.options.KeysScanOptions;

import java.time.Duration;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ClusterNodeCounterTest {

    @Mock RedissonClient redisson;
    @Mock RKeys rKeys;
    @Mock RBucket<String> bucket;

    private final ClusterKeyBuilder keys = new ClusterKeyBuilder("test-app");
    private ClusterNodeCounter counter;

    @BeforeEach
    void setUp() {
        counter = new ClusterNodeCounter(redisson, keys);
    }

    @Test
    void shouldRegisterNodeWithTtl() {
        when(redisson.<String>getBucket(keys.nodeKey("n1"))).thenReturn(bucket);

        counter.registerNode("n1", 60);

        verify(bucket).set("alive");
        verify(bucket).expire(Duration.ofSeconds(60));
    }

    @Test
    void shouldRefreshTtlWhenNodeAlreadyExists() {
        when(redisson.<String>getBucket(anyString())).thenReturn(bucket);
        when(bucket.isExists()).thenReturn(true);

        counter.refreshHeartbeat("n1", 60);

        verify(bucket).expire(Duration.ofSeconds(60));
        verify(bucket, never()).set(anyString());
    }

    @Test
    void shouldReRegisterWhenNodeDoesNotExist() {
        when(redisson.<String>getBucket(anyString())).thenReturn(bucket);
        when(bucket.isExists()).thenReturn(false);

        counter.refreshHeartbeat("n1", 45);

        verify(bucket).set("alive");
        verify(bucket).expire(Duration.ofSeconds(45));
    }

    @Test
    void shouldDeregisterNode() {
        when(redisson.<String>getBucket(keys.nodeKey("n1"))).thenReturn(bucket);

        counter.deregisterNode("n1");

        verify(bucket).delete();
    }

    @Test
    void shouldCountAllAliveNodes() {
        when(redisson.getKeys()).thenReturn(rKeys);
        when(rKeys.getKeysStream(any(KeysScanOptions.class)))
                .thenReturn(Stream.of("k1", "k2", "k3"));

        long count = counter.countAllAliveNodes();

        assertThat(count).isEqualTo(3);
    }

    @Test
    void shouldCountOtherAliveNodesExcludingSelf() {
        when(redisson.getKeys()).thenReturn(rKeys);
        String selfKey = keys.nodeKey("self");
        when(rKeys.getKeysStream(any(KeysScanOptions.class)))
                .thenReturn(Stream.of(
                        keys.nodeKey("n1"),
                        keys.nodeKey("n2"),
                        selfKey
                ));

        long count = counter.countOtherAliveNodes("self");

        assertThat(count).isEqualTo(2);
    }

    @Test
    void shouldReturnZeroWhenNoOtherNodes() {
        when(redisson.getKeys()).thenReturn(rKeys);
        when(rKeys.getKeysStream(any(KeysScanOptions.class)))
                .thenReturn(Stream.of(keys.nodeKey("self")));

        long count = counter.countOtherAliveNodes("self");

        assertThat(count).isZero();
    }

    @Test
    void shouldCheckIfNodeIsAlive() {
        when(redisson.<String>getBucket(keys.nodeKey("n1"))).thenReturn(bucket);
        when(bucket.isExists()).thenReturn(true);

        assertThat(counter.isAlive("n1")).isTrue();
    }

    @Test
    void shouldReturnFalseWhenNodeIsDead() {
        when(redisson.<String>getBucket(keys.nodeKey("n1"))).thenReturn(bucket);
        when(bucket.isExists()).thenReturn(false);

        assertThat(counter.isAlive("n1")).isFalse();
    }
}