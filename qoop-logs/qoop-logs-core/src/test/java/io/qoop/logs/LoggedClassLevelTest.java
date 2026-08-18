package io.qoop.infrastructure.logger;

import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.read.ListAppender;
import io.qoop.logs.LoggedAspect;
import io.qoop.logs.QoopDomainLogger;
import io.qoop.logs.annotation.Logged;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

@Logged("CLASS_LEVEL_TEST_KEY")
class LoggedClassLevelTest {

    private QoopDomainLogger domainLogger;
    private ListAppender<ILoggingEvent> listAppender;

    @BeforeEach
    void setUp() {
        domainLogger = new QoopDomainLogger();
        MDC.clear();

        Logger logger = (Logger) LoggerFactory.getLogger(LoggedClassLevelTest.class);
        listAppender = new ListAppender<>();
        listAppender.start();
        logger.addAppender(listAppender);
    }

    @Test
    @DisplayName("Should resolve log key from class level annotation when method lacks annotation")
    void shouldResolveKeyFromClassLevelAnnotation() {
        domainLogger.info("Executing method in class annotated with @Logged");

        assertEquals(1, listAppender.list.size());
        ILoggingEvent event = listAppender.list.getFirst();

        assertEquals("CLASS_LEVEL_TEST_KEY", event.getMDCPropertyMap().get(LoggedAspect.MDC_LOG_KEY));
        assertNull(MDC.get(LoggedAspect.MDC_LOG_KEY));
    }
}