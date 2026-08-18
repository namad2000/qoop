package io.qoop.global.config;

import io.qoop.global.log.NoOpDomainLogger;
import io.qoop.logs.DomainLogger;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Fallback;

@Configuration
public class DomainLoggerAutoConfiguration {

    @Bean
    @Fallback
    public DomainLogger fallbackDomainLogger() {
        return NoOpDomainLogger.INSTANCE;
    }
}