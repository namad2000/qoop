package io.qoop.cluster.integration;

import io.qoop.cluster.*;
import io.qoop.cluster.integration.support.EmbeddedRedisTestConfiguration;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.util.ReflectionTestUtils;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

@SpringBootTest(classes = {
        ClusterKeyBuilder.class,
        NodeIdentity.class,
        ClusterNodeCounter.class,
        ClusterRoleDetector.class,
        NodeLifecycle.class
})
@Import(EmbeddedRedisTestConfiguration.class)
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
class ClusterEmbeddedIntegrationTest {

    @Autowired
    private RedissonClient redissonClient;

    @Autowired
    private ClusterKeyBuilder keyBuilder;

    @Autowired
    private NodeIdentity nodeIdentity;

    @Autowired
    private ClusterNodeCounter nodeCounter;

    @Autowired
    private ClusterRoleDetector roleDetector;

    @Autowired
    private NodeLifecycle nodeLifecycle;

    @BeforeEach
    void setUp() {
        Thread.interrupted();
        redissonClient.getKeys().flushdb();
    }

    @AfterEach
    void tearDown() {
        Thread.interrupted();
        redissonClient.getKeys().flushdb();
    }

    // ==========================================
    // ClusterKeyBuilder Tests
    // ==========================================

    @Test
    @DisplayName("ClusterKeyBuilder: Default fallback app name and key formatting")
    void testKeyBuilderDefaults() {
        ClusterKeyBuilder defaultKeyBuilder = new ClusterKeyBuilder("app");

        assertThat(defaultKeyBuilder.nodeKey("node1"))
                .isEqualTo("qoop:app:cluster:node:node1");
        assertThat(defaultKeyBuilder.nodeKeyScanPattern())
                .isEqualTo("qoop:app:cluster:node:*");
        assertThat(defaultKeyBuilder.masterLockKey("job1"))
                .isEqualTo("qoop:app:cluster:master-lock:job1");
        assertThat(defaultKeyBuilder.generationKey("job1"))
                .isEqualTo("qoop:app:cluster:generation:job1");
    }

    // ==========================================
    // NodeIdentity Tests
    // ==========================================

    @Test
    @DisplayName("NodeIdentity: Exception branch coverage in buildNodeId")
    void testNodeIdentityExceptionHandling() {
        try (MockedStatic<InetAddress> mockedInet = mockStatic(InetAddress.class)) {
            mockedInet.when(InetAddress::getLocalHost).thenThrow(new UnknownHostException("Host unresolvable"));

            NodeIdentity fallbackIdentity = new NodeIdentity("fallbackApp");
            assertThat(fallbackIdentity.getNodeId()).startsWith("fallbackApp@");
        }
    }

    // ==========================================
    // ClusterNodeCounter Tests
    // ==========================================

    @Test
    @DisplayName("ClusterNodeCounter: Full lifecycle, heartbeat refresh branches, and scanning")
    void testClusterNodeCounterBranches() throws InterruptedException {
        String nodeId = "node-alpha";

        // Register new node
        nodeCounter.registerNode(nodeId, 5);
        assertThat(nodeCounter.isAlive(nodeId)).isTrue();

        // Refresh heartbeat when node EXISTS (true branch)
        nodeCounter.refreshHeartbeat(nodeId, 5);
        assertThat(nodeCounter.isAlive(nodeId)).isTrue();

        // Deregister node
        nodeCounter.deregisterNode(nodeId);
        assertThat(nodeCounter.isAlive(nodeId)).isFalse();

        // Refresh heartbeat when node DOES NOT EXIST (false branch -> calls registerNode)
        nodeCounter.refreshHeartbeat(nodeId, 5);
        assertThat(nodeCounter.isAlive(nodeId)).isTrue();

        // Scan and count coverage
        nodeCounter.registerNode("node-beta", 5);
        assertThat(nodeCounter.countAllAliveNodes()).isEqualTo(2);
        assertThat(nodeCounter.countOtherAliveNodes(nodeId)).isEqualTo(1);

        // TTL expiration check
        nodeCounter.registerNode("temp-node", 1);
        Thread.sleep(1200);
        assertThat(nodeCounter.isAlive("temp-node")).isFalse();
    }

    // ==========================================
    // ClusterRoleDetector Tests
    // ==========================================

    @Test
    @DisplayName("ClusterRoleDetector: Win election, lost election with active master, and release lock")
    void testMasterElectionFlows() throws Exception {
        String jobName = "export-job";
        long lockTtl = 5;

        // Initial generation check when key does not exist (returns 0L)
        assertThat(roleDetector.currentGeneration(jobName)).isEqualTo(0L);

        // Thread 1: Acquire leadership (Won election)
        MasterElectionResult winResult = roleDetector.tryBecomeMaster(jobName, lockTtl);
        assertThat(winResult.isMaster()).isTrue();
        assertThat(winResult.generation()).isGreaterThan(0L);
        assertThat(winResult.currentMaster()).isNull();
        assertThat(roleDetector.isMaster(jobName)).isTrue();

        // Thread 2: Try acquiring lock while Thread 1 holds it (Lost election branch)
        CountDownLatch latch = new CountDownLatch(1);
        MasterElectionResult[] lostResultHolder = new MasterElectionResult[1];

        Thread secondaryThread = new Thread(() -> {
            lostResultHolder[0] = roleDetector.tryBecomeMaster(jobName, lockTtl);
            latch.countDown();
        });
        secondaryThread.start();
        latch.await(3, TimeUnit.SECONDS);

        assertThat(lostResultHolder[0].isMaster()).isFalse();
        assertThat(lostResultHolder[0].generation()).isEqualTo(0L);
        assertThat(lostResultHolder[0].currentMaster()).startsWith("master@");

        // Release lock from non-owner thread (isHeldByCurrentThread -> false branch)
        secondaryThread = new Thread(() -> roleDetector.releaseMaster(jobName));
        secondaryThread.start();
        secondaryThread.join();

        // Release lock from owner thread (isHeldByCurrentThread -> true branch)
        roleDetector.releaseMaster(jobName);
        assertThat(roleDetector.isMaster(jobName)).isFalse();
    }

    @Test
    @DisplayName("ClusterRoleDetector: Lost election when master generation is expired or unknown")
    void testLostElectionWithUnknownMaster() throws Exception {
        String jobName = "unknown-master-job";
        RedissonClient mockRedisson = mock(RedissonClient.class);
        RLock mockLock = mock(RLock.class);

        when(mockRedisson.getLock(anyString())).thenReturn(mockLock);
        when(mockLock.tryLock(anyLong(), anyLong(), any())).thenReturn(false);

        @SuppressWarnings("unchecked")
        org.redisson.api.RBucket<Object> mockBucket = mock(org.redisson.api.RBucket.class);
        when(mockRedisson.getBucket(anyString())).thenReturn(mockBucket);
        when(mockBucket.isExists()).thenReturn(false);

        ClusterRoleDetector detector = new ClusterRoleDetector(mockRedisson, keyBuilder);
        MasterElectionResult result = detector.tryBecomeMaster(jobName, 5);

        assertThat(result.isMaster()).isFalse();
        assertThat(result.currentMaster()).isEqualTo("unknown");
    }

    @Test
    @DisplayName("ClusterRoleDetector: InterruptedException handling branch")
    void testMasterElectionInterruptedException() throws Exception {
        RedissonClient mockRedisson = mock(RedissonClient.class);
        RLock mockLock = mock(RLock.class);

        when(mockRedisson.getLock(anyString())).thenReturn(mockLock);
        when(mockLock.tryLock(anyLong(), anyLong(), any())).thenThrow(new InterruptedException("Thread interrupted"));

        ClusterRoleDetector detector = new ClusterRoleDetector(mockRedisson, keyBuilder);
        MasterElectionResult result = detector.tryBecomeMaster("interrupted-job", 5);

        assertThat(result.isMaster()).isFalse();
        assertThat(result.currentMaster()).isNull();
        assertThat(Thread.currentThread().isInterrupted()).isTrue();

        // Clear interrupted status so tearDown() redisson.flushdb() doesn't fail
        Thread.interrupted();
    }

    // ==========================================
    // NodeLifecycle Tests
    // ==========================================

    @Test
    @DisplayName("NodeLifecycle: Normal startup, heartbeat, shutdown, and exception catch blocks")
    void testNodeLifecycleExceptionsAndMethods() {
        // Test standard lifecycle methods
        nodeLifecycle.onStart();
        assertThat(nodeCounter.isAlive(nodeIdentity.getNodeId())).isTrue();

        nodeLifecycle.refreshHeartbeat();
        assertThat(nodeCounter.isAlive(nodeIdentity.getNodeId())).isTrue();

        nodeLifecycle.onShutdown();
        assertThat(nodeCounter.isAlive(nodeIdentity.getNodeId())).isFalse();

        // Test Exception handling branches in refreshHeartbeat and onShutdown
        ClusterNodeCounter mockCounter = mock(ClusterNodeCounter.class);
        doThrow(new RuntimeException("Redis connection error"))
                .when(mockCounter).refreshHeartbeat(anyString(), anyLong());
        doThrow(new RuntimeException("Redis connection error"))
                .when(mockCounter).deregisterNode(anyString());

        NodeLifecycle mockLifecycle = new NodeLifecycle(nodeIdentity, mockCounter);
        ReflectionTestUtils.setField(mockLifecycle, "ttlSeconds", 60L);

        // Ensures catch blocks are executed without throwing unhandled exceptions
        mockLifecycle.refreshHeartbeat();
        mockLifecycle.onShutdown();
    }
}