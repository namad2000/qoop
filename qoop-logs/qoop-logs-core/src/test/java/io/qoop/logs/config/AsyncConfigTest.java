package io.qoop.logs.config;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.slf4j.MDC;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

import static io.qoop.logs.LogKeys.MDC_KEY;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@ExtendWith(SpringExtension.class)
@TestPropertySource(properties = {
        "async.core-pool-size=1",
        "async.max-pool-size=1",
        "async.queue-capacity=10",
        "async.thread-name-prefix=Test-Async-"
})
@ContextConfiguration(classes = AsyncConfig.class)
class AsyncConfigTest {

    @Autowired
    @Qualifier("taskExecutor")
    private Executor executor;

    @Test
    void taskExecutor_shouldPropagateMdcContext() throws Exception {
        assertTrue(executor instanceof ThreadPoolTaskExecutor);

        MDC.put(MDC_KEY, "test-id");

        CountDownLatch latch = new CountDownLatch(1);
        AtomicReference<String> mdcValue = new AtomicReference<>();

        executor.execute(() -> {
            mdcValue.set(MDC.get(MDC_KEY));
            latch.countDown();
        });

        assertTrue(latch.await(2, TimeUnit.SECONDS));
        assertEquals("test-id", mdcValue.get());

        MDC.clear();
    }
}
