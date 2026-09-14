package io.qoop.cluster;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RBucket;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.concurrent.TimeUnit;

/**
 * Distributed master election using Redisson locks.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class ClusterRoleDetector {

    private final RedissonClient redisson;
    private final ClusterKeyBuilder keys;

    /**
     * Attempts to acquire leadership for the given resource key.
     */
    public MasterElectionResult tryBecomeMaster(String resourceName, long lockSeconds) {
        RLock lock = redisson.getLock(keys.masterLockKey(resourceName));

        try {
            boolean acquired = lock.tryLock(0, lockSeconds, TimeUnit.SECONDS);

            if (acquired) {
                long generation = System.currentTimeMillis();
                storeGeneration(resourceName, generation, lockSeconds);

                log.info("Became MASTER for resource [{}] with generation [{}]",
                        resourceName, generation);
                return MasterElectionResult.won(generation);
            } else {
                String currentMaster = readCurrentMaster(resourceName);
                log.info("Resource [{}] already has a MASTER [{}] - acting as worker",
                        resourceName, currentMaster);
                return MasterElectionResult.lost(currentMaster);
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            log.warn("Master election interrupted for resource [{}]", resourceName);
            return MasterElectionResult.lost(null);
        }
    }

    /**
     * Releases leadership for the given resource key.
     */
    public void releaseMaster(String resourceName) {
        RLock lock = redisson.getLock(keys.masterLockKey(resourceName));
        if (lock.isHeldByCurrentThread()) {
            lock.unlock();
            log.info("Released MASTER lock for resource [{}]", resourceName);
        }
    }

    /**
     * Checks whether the current thread holds the master lock for the given resource.
     */
    public boolean isMaster(String resourceName) {
        RLock lock = redisson.getLock(keys.masterLockKey(resourceName));
        return lock.isHeldByCurrentThread();
    }

    /**
     * Returns the current fencing generation for the given resource.
     */
    public long currentGeneration(String resourceName) {
        RBucket<Long> bucket = redisson.getBucket(keys.generationKey(resourceName));
        Long value = bucket.get();
        return value != null ? value : 0L;
    }

    /**
     * Stores the fencing generation for the master lock.
     */
    private void storeGeneration(String resourceName, long generation, long ttlSeconds) {
        RBucket<Long> bucket = redisson.getBucket(keys.generationKey(resourceName));
        bucket.set(generation);
        bucket.expire(Duration.ofSeconds(ttlSeconds));
    }

    private String readCurrentMaster(String resourceName) {
        RBucket<Long> bucket = redisson.getBucket(keys.generationKey(resourceName));
        return bucket.isExists() ? "master@" + bucket.get() : "unknown";
    }
}