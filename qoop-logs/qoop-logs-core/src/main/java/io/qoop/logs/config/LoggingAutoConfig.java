package io.qoop.logs.config;

import ch.qos.logback.classic.AsyncAppender;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.encoder.PatternLayoutEncoder;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.ConsoleAppender;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

@Configuration
public class LoggingAutoConfig {

    /**
     * Read the log pattern from application.yml.
     * If not provided, use the default pattern with Trace and Correlation IDs.
     */
    @Value("${logging.pattern.console:[%d{yyyy-MM-dd HH:mm:ss.SSS}] [%thread] %-5level [Corr: %X{correlationId:-}] [Trace: %X{traceId:-}, Span: %X{spanId:-}] [Key: %X{logKey:-}] %logger{36} - %msg%n}")
    private String logPattern;

    @PostConstruct
    public void configureAsyncConsole() {
        LoggerContext context = (LoggerContext) LoggerFactory.getILoggerFactory();
        Logger rootLogger = context.getLogger(org.slf4j.Logger.ROOT_LOGGER_NAME);

        // 1. Initialize the Encoder with the injected or default pattern
        PatternLayoutEncoder encoder = new PatternLayoutEncoder();
        encoder.setContext(context);
        encoder.setPattern(logPattern);
        encoder.start();

        // 2. Initialize the Console Appender
        ConsoleAppender<ILoggingEvent> consoleAppender = new ConsoleAppender<>();
        consoleAppender.setContext(context);
        consoleAppender.setName("INTERNAL_CONSOLE");
        consoleAppender.setEncoder(encoder);
        consoleAppender.start();

        // 3. Setup Async Appender to wrap the Console Appender for better performance
        AsyncAppender asyncAppender = new AsyncAppender();
        asyncAppender.setContext(context);
        asyncAppender.setName("ASYNC_CONSOLE");
        asyncAppender.setQueueSize(512);
        asyncAppender.setDiscardingThreshold(0);

        asyncAppender.setIncludeCallerData(true);

        asyncAppender.addAppender(consoleAppender);
        asyncAppender.start();

        // 4. Clear existing appenders and attach the new Async Console appender
        rootLogger.detachAndStopAllAppenders();
        rootLogger.addAppender(asyncAppender);
    }

    @PreDestroy
    public void stopLogging() {
        LoggerContext context = (LoggerContext) LoggerFactory.getILoggerFactory();
        context.stop();
    }
}
