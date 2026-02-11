package io.qoop.checker;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.autoconfigure.AutoConfigureOrder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;

@Slf4j
@Configuration
@AutoConfigureOrder(Ordered.HIGHEST_PRECEDENCE)
public class VirtualThreadChecker {

    @Bean
    public ApplicationRunner checkVirtualThreads() {
        return args -> {
            Thread testThread = Thread.ofVirtual().start(() -> {
            });

            boolean isVirtualEnabled = testThread.isVirtual();

            if (isVirtualEnabled) {
                log.info("✅ Virtual Threads are ENABLED and working correctly.");
            } else {
                log.error("❌ CRITICAL: Virtual Threads are NOT enabled! Check your configuration.");
                log.warn("Ensure you are using Java 21+ and Spring Boot 3.2+");
            }
        };
    }
}
