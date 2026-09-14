package io.qoop.cluster.integration.support;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import org.redisson.Redisson;
import org.redisson.api.RedissonClient;
import org.redisson.config.Config;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import redis.embedded.RedisServer;

import java.io.IOException;
import java.net.ServerSocket;

@TestConfiguration
public class EmbeddedRedisTestConfiguration {

    private static RedisServer redisServer;
    private static int redisPort;

    @PostConstruct
    public synchronized void startRedis() throws IOException {
        if (redisServer == null || !redisServer.isActive()) {
            redisPort = findFreePort();
            redisServer = RedisServer.builder()
                    .port(redisPort)
                    .setting("maxmemory 128M")
                    .build();
            redisServer.start();
        }
    }

    private int findFreePort() throws IOException {
        try (ServerSocket socket = new ServerSocket(0)) {
            socket.setReuseAddress(true);
            return socket.getLocalPort();
        }
    }

    @Bean(destroyMethod = "shutdown")
    public RedissonClient redissonClient() {
        Config config = new Config();
        config.useSingleServer()
                .setAddress("redis://127.0.0.1:" + redisPort)
                .setConnectionMinimumIdleSize(1)
                .setConnectionPoolSize(5);
        return Redisson.create(config);
    }

    @PreDestroy
    public synchronized void stopRedis() {
        if (redisServer != null && redisServer.isActive()) {
            redisServer.stop();
            redisServer = null;
        }
    }
}