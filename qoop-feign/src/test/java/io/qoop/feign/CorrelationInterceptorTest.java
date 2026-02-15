package io.qoop.feign;

import feign.RequestTemplate;
import io.qoop.logs.LogKeys;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.slf4j.MDC;

import static org.assertj.core.api.Assertions.assertThat;

class CorrelationInterceptorTest {

    private final CorrelationInterceptor interceptor = new CorrelationInterceptor();

    @AfterEach
    void tearDown() {
        MDC.clear();
    }

    @Test
    void should_add_header_when_correlation_id_exists() {
        // given
        MDC.put(LogKeys.MDC_KEY, "corr-123");
        RequestTemplate template = new RequestTemplate();

        // when
        interceptor.apply(template);

        // then
        assertThat(template.headers())
                .containsKey(LogKeys.CORRELATION_ID_HEADER);

        assertThat(template.headers()
                .get(LogKeys.CORRELATION_ID_HEADER))
                .containsExactly("corr-123");
    }

    @Test
    void should_not_add_header_when_correlation_id_is_null() {
        // given
        RequestTemplate template = new RequestTemplate();

        // when
        interceptor.apply(template);

        // then
        assertThat(template.headers())
                .doesNotContainKey(LogKeys.CORRELATION_ID_HEADER);
    }

    @Test
    void should_not_add_header_when_correlation_id_is_empty() {
        // given
        MDC.put(LogKeys.MDC_KEY, "");
        RequestTemplate template = new RequestTemplate();

        // when
        interceptor.apply(template);

        // then
        assertThat(template.headers())
                .doesNotContainKey(LogKeys.CORRELATION_ID_HEADER);
    }
}
