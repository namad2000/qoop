package io.qoop.logs.config;

import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.LoggerContext;
import org.junit.jupiter.api.Test;
import org.slf4j.LoggerFactory;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Author: davood akbari
 * Email: daak1365@gmail.com
 * Created: 1/4/2026 1:14 PM
 * Package: io.qoop.logs
 */

@SpringBootTest(
        classes = LoggingAutoConfig.class,
        properties = {
                "logging.pattern.console=[TEST] %msg%n"
        }
)
class LoggingAutoConfigTest {

    @Test
    void shouldConfigureAsyncConsoleAppender() {
        LoggerContext context = (LoggerContext) LoggerFactory.getILoggerFactory();

        Logger rootLogger = context.getLogger(Logger.ROOT_LOGGER_NAME);

        assertTrue(
                rootLogger.iteratorForAppenders()
                        .hasNext()
        );
    }
}
