package io.qoop.cluster;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.redisson.api.RBucket;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;

import java.time.Duration;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ClusterRoleDetectorTest {

    @Mock RedissonClient redisson;
    @Mock RLock lock;
    @Mock RBucket<Long> generationBucket;

    private final ClusterKeyBuilder keys = new ClusterKeyBuilder("test-app");
    private ClusterRoleDetector detector;

    @BeforeEach
    void setUp() {
        detector = new ClusterRoleDetector(redisson, keys);
    }

    @Test
    void shouldBecomeMasterWhenLockAcquired() throws InterruptedException {
        when(redisson.getLock(keys.masterLockKey("job1"))).thenReturn(lock);
        when(redisson.<Long>getBucket(keys.generationKey("job1"))).thenReturn(generationBucket);
        when(lock.tryLock(0, 30, TimeUnit.SECONDS)).thenReturn(true);

        MasterElectionResult result = detector.tryBecomeMaster("job1", 30);

        assertThat(result.isMaster()).isTrue();
        assertThat(result.generation()).isGreaterThan(0);
        assertThat(result.currentMaster()).isNull();
        verify(generationBucket).set(anyLong());
        verify(generationBucket).expire(Duration.ofSeconds(30));
    }

    @Test
    void shouldReturnLostWhenLockNotAcquired() throws InterruptedException {
        when(redisson.getLock(anyString())).thenReturn(lock);
        when(redisson.<Long>getBucket(anyString())).thenReturn(generationBucket);
        when(lock.tryLock(anyLong(), anyLong(), any(TimeUnit.class))).thenReturn(false);
        when(generationBucket.isExists()).thenReturn(true);
        when(generationBucket.get()).thenReturn(12345L);

        MasterElectionResult result = detector.tryBecomeMaster("job1", 30);

        assertThat(result.isMaster()).isFalse();
        assertThat(result.generation()).isZero();
        assertThat(result.currentMaster()).isEqualTo("master@12345");
        verify(generationBucket, never()).set(anyLong());
    }

    @Test
    void shouldReturnLostWithUnknownMasterWhenNoGenerationExists() throws InterruptedException {
        when(redisson.getLock(anyString())).thenReturn(lock);
        when(redisson.<Long>getBucket(anyString())).thenReturn(generationBucket);
        when(lock.tryLock(anyLong(), anyLong(), any(TimeUnit.class))).thenReturn(false);
        when(generationBucket.isExists()).thenReturn(false);

        MasterElectionResult result = detector.tryBecomeMaster("job1", 30);

        assertThat(result.isMaster()).isFalse();
        assertThat(result.currentMaster()).isEqualTo("unknown");
    }

    @Test
    void shouldReturnLostOnInterruptedException() throws InterruptedException {
        when(redisson.getLock(anyString())).thenReturn(lock);
        when(lock.tryLock(anyLong(), anyLong(), any(TimeUnit.class)))
                .thenThrow(new InterruptedException());

        MasterElectionResult result = detector.tryBecomeMaster("job1", 30);

        assertThat(result.isMaster()).isFalse();
        assertThat(Thread.interrupted()).isTrue(); // verify interrupt flag restored
    }

    @Test
    void shouldReleaseLockWhenHeldByCurrentThread() {
        when(redisson.getLock(anyString())).thenReturn(lock);
        when(lock.isHeldByCurrentThread()).thenReturn(true);

        detector.releaseMaster("job1");

        verify(lock).unlock();
    }

    @Test
    void shouldNotReleaseLockWhenNotHeldByCurrentThread() {
        when(redisson.getLock(anyString())).thenReturn(lock);
        when(lock.isHeldByCurrentThread()).thenReturn(false);

        detector.releaseMaster("job1");

        verify(lock, never()).unlock();
    }

    @Test
    void shouldReportMasterStatus() {
        when(redisson.getLock(anyString())).thenReturn(lock);
        when(lock.isHeldByCurrentThread()).thenReturn(true);

        assertThat(detector.isMaster("job1")).isTrue();
    }

    @Test
    void shouldReturnZeroGenerationWhenNoMasterElected() {
        when(redisson.<Long>getBucket(anyString())).thenReturn(generationBucket);
        when(generationBucket.get()).thenReturn(null);

        assertThat(detector.currentGeneration("job1")).isZero();
    }

    @Test
    void shouldReturnStoredGeneration() {
        when(redisson.<Long>getBucket(anyString())).thenReturn(generationBucket);
        when(generationBucket.get()).thenReturn(98765L);

        assertThat(detector.currentGeneration("job1")).isEqualTo(98765L);
    }
}