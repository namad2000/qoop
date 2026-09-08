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
     * Attempts to acquire leadership for the given job key.
     */
    public MasterElectionResult tryBecomeMaster(String jobName, long lockSeconds) {
        RLock lock = redisson.getLock(keys.masterLockKey(jobName));

        try {
            boolean acquired = lock.tryLock(0, lockSeconds, TimeUnit.SECONDS);

            if (acquired) {
                long generation = System.currentTimeMillis();
                storeGeneration(jobName, generation, lockSeconds);

                log.info("Became MASTER for job [{}] with generation [{}]",
                        jobName, generation);
                return MasterElectionResult.won(generation);
            } else {
                String currentMaster = readCurrentMaster(jobName);
                log.info("Job [{}] already has a MASTER [{}] - acting as worker",
                        jobName, currentMaster);
                return MasterElectionResult.lost(currentMaster);
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            log.warn("Master election interrupted for job [{}]", jobName);
            return MasterElectionResult.lost(null);
        }
    }

    /**
     * Releases leadership for the given job key.
     */
    public void releaseMaster(String jobName) {
        RLock lock = redisson.getLock(keys.masterLockKey(jobName));
        if (lock.isHeldByCurrentThread()) {
            lock.unlock();
            log.info("Released MASTER lock for job [{}]", jobName);
        }
    }

    /**
     * Checks whether the current thread holds the master lock for the given job.
     */
    public boolean isMaster(String jobName) {
        RLock lock = redisson.getLock(keys.masterLockKey(jobName));
        return lock.isHeldByCurrentThread();
    }

    /**
     * Returns the current fencing generation for the given job.
     */
    public long currentGeneration(String jobName) {
        RBucket<Long> bucket = redisson.getBucket(keys.generationKey(jobName));
        Long value = bucket.get();
        return value != null ? value : 0L;
    }

    /**
     * Stores the fencing generation for the master lock.
     */
    private void storeGeneration(String jobName, long generation, long ttlSeconds) {
        RBucket<Long> bucket = redisson.getBucket(keys.generationKey(jobName));
        bucket.set(generation);
        bucket.expire(Duration.ofSeconds(ttlSeconds));
    }

    private String readCurrentMaster(String jobName) {
        RBucket<Long> bucket = redisson.getBucket(keys.generationKey(jobName));
        return bucket.isExists() ? "master@" + bucket.get() : "unknown";
    }
}