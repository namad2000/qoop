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
}