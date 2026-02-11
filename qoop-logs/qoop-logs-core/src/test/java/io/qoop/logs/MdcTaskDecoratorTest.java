package io.qoop.logs;

import org.junit.jupiter.api.Test;
import org.slf4j.MDC;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

class MdcTaskDecoratorTest {

    @Test
    void shouldCopyMdcContextToNewThread() {
        MDC.put("key", "value");

        MdcTaskDecorator decorator = new MdcTaskDecorator();

        Runnable decorated = decorator.decorate(() -> {
            assertEquals("value", MDC.get("key"));
        });

        decorated.run();

        assertNull(MDC.get("key"));
    }
}
