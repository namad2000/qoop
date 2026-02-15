package io.qoop.feign;

import feign.RequestInterceptor;
import feign.RequestTemplate;
import io.qoop.logs.LogKeys;
import org.slf4j.MDC;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

@Component
@Order(Ordered.HIGHEST_PRECEDENCE)
public class CorrelationInterceptor implements RequestInterceptor {

    @Override
    public void apply(RequestTemplate template) {
        String correlationId = MDC.get(LogKeys.MDC_KEY);
        if (correlationId != null && !correlationId.isEmpty()) {
            template.header(LogKeys.CORRELATION_ID_HEADER, correlationId);
        }
    }
}