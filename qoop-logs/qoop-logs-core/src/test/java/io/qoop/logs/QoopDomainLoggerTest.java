package io.qoop.logs;

import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.read.ListAppender;
import io.qoop.logs.annotation.Logged;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

class QoopDomainLoggerTest {

    private DomainLogger domainLogger;
    private ListAppender<ILoggingEvent> listAppender;

    @BeforeEach
    void setUp() {
        domainLogger = new QoopDomainLogger();
        MDC.clear();

        Logger logger = (Logger) LoggerFactory.getLogger(QoopDomainLoggerTest.class);
        listAppender = new ListAppender<>();
        listAppender.start();
        logger.addAppender(listAppender);
    }

    @Test
    @DisplayName("Should resolve key from method level annotation and clean up MDC")
    @Logged("TEST_METHOD_KEY")
    void shouldResolveKeyFromAnnotationOnMethod() {
        domainLogger.info("Message inside annotated method");

        assertEquals(1, listAppender.list.size());
        ILoggingEvent event = listAppender.list.getFirst();

        assertEquals("TEST_METHOD_KEY", event.getMDCPropertyMap().get(LoggedAspect.MDC_LOG_KEY));
        assertNull(MDC.get(LoggedAspect.MDC_LOG_KEY));
    }

    @Test
    @DisplayName("Should use explicit key when infoWithKey is called")
    void shouldUseExplicitKeyWhenProvided() {
        domainLogger.infoWithKey("EXPLICIT_KEY", "Message with explicit key");

        assertEquals(1, listAppender.list.size());
        ILoggingEvent event = listAppender.list.getFirst();

        assertEquals("EXPLICIT_KEY", event.getMDCPropertyMap().get(LoggedAspect.MDC_LOG_KEY));
        assertNull(MDC.get(LoggedAspect.MDC_LOG_KEY));
    }

    @Test
    @DisplayName("Should correctly set MDC key and clean up state after logInfoForClass execution")
    void shouldLogInfoForClassAndMaintainMdcState() {
        domainLogger.logInfoForClass(TargetClass.class, "INFO_KEY", "User logged in: {}", "davood");

        // MDC key must be cleaned up post invocation
        assertNull(MDC.get(LoggedAspect.MDC_LOG_KEY));
    }

    @Test
    @DisplayName("Should correctly set MDC key and clean up state after logWarnForClass execution")
    void shouldLogWarnForClassAndMaintainMdcState() {
        domainLogger.logWarnForClass(TargetClass.class, "WARN_KEY", "Memory threshold reached: {}%", 85);

        assertNull(MDC.get(LoggedAspect.MDC_LOG_KEY));
    }

    @Test
    @DisplayName("Should correctly set MDC key and clean up state after logDebugForClass execution")
    void shouldLogDebugForClassAndMaintainMdcState() {
        domainLogger.logDebugForClass(TargetClass.class, "DEBUG_KEY", "Payload details: {}", "{\"id\":1}");

        assertNull(MDC.get(LoggedAspect.MDC_LOG_KEY));
    }

    @Test
    @DisplayName("Should correctly set MDC key and clean up state after logErrorForClass execution with parameters")
    void shouldLogErrorForClassWithParamsAndMaintainMdcState() {
        domainLogger.logErrorForClass(TargetClass.class, "ERROR_KEY", "Transaction failed for orderId: {}", "ORD-99");

        assertNull(MDC.get(LoggedAspect.MDC_LOG_KEY));
    }

    @Test
    @DisplayName("Should correctly set MDC key and clean up state after logErrorForClass execution with Throwable")
    void shouldLogErrorForClassWithThrowableAndMaintainMdcState() {
        RuntimeException ex = new RuntimeException("Database Connection Timeout");

        domainLogger.logErrorForClass(TargetClass.class, "ERROR_KEY", "Database error occurred", ex);

        assertNull(MDC.get(LoggedAspect.MDC_LOG_KEY));
    }

    @Test
    @DisplayName("Should preserve existing outer MDC context after targeted log completion")
    void shouldPreserveExistingMdcContext() {
        MDC.put(LoggedAspect.MDC_LOG_KEY, "OUTER_KEY");

        domainLogger.logWarnForClass(TargetClass.class, "INNER_KEY", "Warning message");

        // The outer MDC context must be restored to its previous value
        assertEquals("OUTER_KEY", MDC.get(LoggedAspect.MDC_LOG_KEY));
    }

    static class TargetClass {
    }
}