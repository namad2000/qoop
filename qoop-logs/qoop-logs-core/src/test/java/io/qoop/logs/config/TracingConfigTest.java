package io.qoop.logs.config;

import brave.propagation.CurrentTraceContext;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import static org.junit.jupiter.api.Assertions.assertNotNull;

@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = TracingConfig.class)
class TracingConfigTest {

    @Autowired
    private CurrentTraceContext.ScopeDecorator decorator;

    @Test
    void mdcScopeDecoratorBeanShouldExist() {
        assertNotNull(decorator);
    }
}
