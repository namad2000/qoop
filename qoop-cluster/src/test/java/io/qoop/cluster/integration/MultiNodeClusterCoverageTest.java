package io.qoop.cluster.integration;

import io.qoop.cluster.*;
import io.qoop.cluster.integration.support.EmbeddedRedisTestConfiguration;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(classes = {
        ClusterKeyBuilder.class,
        ClusterNodeCounter.class,
        ClusterRoleDetector.class
})
@Import(EmbeddedRedisTestConfiguration.class)
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
class MultiNodeClusterCoverageTest {

    @Autowired
    private RedissonClient redissonClient;

    @Autowired
    private ClusterKeyBuilder keyBuilder;

    @Autowired
    private ClusterNodeCounter nodeCounter;

    @Autowired
    private ClusterRoleDetector roleDetector;

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

    @Test
    @DisplayName("Multi-Node: Concurrent startup and shutdown of multiple distinct nodes")
    void testMultiNodeLifecycleAndCounting() {
        String appName = keyBuilder.getAppName();

        NodeIdentity node1 = new NodeIdentity(appName);
        NodeIdentity node2 = new NodeIdentity(appName);
        NodeIdentity node3 = new NodeIdentity(appName);

        NodeLifecycle lifecycle1 = new NodeLifecycle(node1, nodeCounter);
        NodeLifecycle lifecycle2 = new NodeLifecycle(node2, nodeCounter);
        NodeLifecycle lifecycle3 = new NodeLifecycle(node3, nodeCounter);

        ReflectionTestUtils.setField(lifecycle1, "ttlSeconds", 60L);
        ReflectionTestUtils.setField(lifecycle2, "ttlSeconds", 60L);
        ReflectionTestUtils.setField(lifecycle3, "ttlSeconds", 60L);

        lifecycle1.onStart();
        lifecycle2.onStart();
        lifecycle3.onStart();

        assertThat(nodeCounter.countAllAliveNodes()).isEqualTo(3L);
        assertThat(nodeCounter.countOtherAliveNodes(node1.getNodeId())).isEqualTo(2L);

        lifecycle3.onShutdown();

        assertThat(nodeCounter.countAllAliveNodes()).isEqualTo(2L);

        lifecycle1.onShutdown();
        lifecycle2.onShutdown();
        assertThat(nodeCounter.countAllAliveNodes()).isEqualTo(0L);
    }

    @Test
    @DisplayName("Multi-Node: True multithreaded election race condition")
    void testConcurrentMultiNodeMasterElection() throws InterruptedException {
        String jobName = "concurrent-job";
        long lockTtl = 10;
        int threadCount = 3;

        ExecutorService executor = Executors.newFixedThreadPool(threadCount);
        CountDownLatch startLatch = new CountDownLatch(1);
        CountDownLatch doneLatch = new CountDownLatch(threadCount);

        MasterElectionResult[] results = new MasterElectionResult[threadCount];

        for (int i = 0; i < threadCount; i++) {
            final int index = i;
            executor.submit(() -> {
                try {
                    startLatch.await();
                    results[index] = roleDetector.tryBecomeMaster(jobName, lockTtl);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                } finally {
                    doneLatch.countDown();
                }
            });
        }

        startLatch.countDown();
        doneLatch.await(5, TimeUnit.SECONDS);
        executor.shutdown();

        long winCount = 0;
        long lostCount = 0;

        for (MasterElectionResult result : results) {
            if (result.isMaster()) {
                winCount++;
            } else {
                lostCount++;
                assertThat(result.currentMaster())
                        .satisfies(master -> assertThat(master.startsWith("master@") || master.equals("unknown")).isTrue());
            }
        }

        assertThat(winCount).isEqualTo(1);
        assertThat(lostCount).isEqualTo(threadCount - 1);

        roleDetector.releaseMaster(jobName);
    }
}