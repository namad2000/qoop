package io.qoop.logs;

import brave.context.slf4j.MDCScopeDecorator;
import brave.propagation.CurrentTraceContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class TracingConfig {

    @Bean
    public CurrentTraceContext.ScopeDecorator mdcScopeDecorator() {
        // This section is only to automatically place TraceId and SpanId into MDC
        return MDCScopeDecorator.newBuilder().build();
    }
}
