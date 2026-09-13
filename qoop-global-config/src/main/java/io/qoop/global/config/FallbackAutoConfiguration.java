package io.qoop.global.config;

import io.qoop.global.log.NoOpDomainLogger;
import io.qoop.global.outbox.NoOpOutboxEventPublisher;
import io.qoop.logs.DomainLogger;
import io.qoop.outbox.OutboxEventPublisher;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Fallback;

@Configuration
public class FallbackAutoConfiguration {

    @Bean
    @Fallback
    public DomainLogger fallbackDomainLogger() {
        return NoOpDomainLogger.INSTANCE;
    }

    @Bean
    @Fallback
    public OutboxEventPublisher fallbackOutboxEventPublisher() {
        return NoOpOutboxEventPublisher.INSTANCE;
    }
}