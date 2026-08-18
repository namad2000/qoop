package io.qoop.logs;

import io.qoop.logs.annotation.Logged;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.stereotype.Component;

import java.lang.StackWalker.StackFrame;
import java.lang.reflect.Method;
import java.util.Optional;

/**
 * Thread-safe implementation of DomainLogger that resolves MDC log keys
 * using explicit values, call-stack inspection, or @Logged annotations.
 */
@Component
public class QoopDomainLogger implements DomainLogger {

    private static final StackWalker WALKER = StackWalker.getInstance(
            StackWalker.Option.RETAIN_CLASS_REFERENCE
    );

    // =========================================================================
    // Public API Implementation
    // =========================================================================

    @Override
    public void info(String message, Object... params) {
        log(Level.INFO, message, params);
    }

    @Override
    public void infoWithKey(String logKey, String message, Object... params) {
        logWithKey(Level.INFO, logKey, message, params);
    }

    @Override
    public void warn(String message, Object... params) {
        log(Level.WARN, message, params);
    }

    @Override
    public void warnWithKey(String logKey, String message, Object... params) {
        logWithKey(Level.WARN, logKey, message, params);
    }

    @Override
    public void debug(String message, Object... params) {
        log(Level.DEBUG, message, params);
    }

    @Override
    public void debugWithKey(String logKey, String message, Object... params) {
        logWithKey(Level.DEBUG, logKey, message, params);
    }

    @Override
    public void error(String message, Throwable throwable) {
        logError(null, message, throwable);
    }

    @Override
    public void errorWithKey(String logKey, String message, Throwable throwable) {
        logError(logKey, message, throwable);
    }

    @Override
    public void logForClass(Class<?> targetClass, String logKey, String message, Object... params) {
        log(Level.INFO, targetClass, logKey, message, params, null);
    }

    // =========================================================================
    // Overloaded Private Helper Methods
    // =========================================================================

    /**
     * Core logging execution when Class and logKey are explicitly specified (e.g., invoked via Aspect).
     */
    private void log(Level level, Class<?> loggerClass, String logKey, String message, Object[] params, Throwable throwable) {
        String previousMdcKey = MDC.get(LoggedAspect.MDC_LOG_KEY);

        String activeKey = logKey;
        if (activeKey == null) {
            activeKey = previousMdcKey;
        }
        if (activeKey == null) {
            activeKey = resolveLoggedKeyFromAnnotation(loggerClass, null);
        }

        MDC.put(LoggedAspect.MDC_LOG_KEY, activeKey);
        Logger logger = LoggerFactory.getLogger(loggerClass);

        try {
            switch (level) {
                case INFO -> logger.info(message, params);
                case WARN -> logger.warn(message, params);
                case DEBUG -> logger.debug(message, params);
                case ERROR -> {
                    if (throwable != null) {
                        logger.error(message, throwable);
                    } else {
                        logger.error(message, params);
                    }
                }
            }
        } finally {
            if (previousMdcKey != null) {
                MDC.put(LoggedAspect.MDC_LOG_KEY, previousMdcKey);
            } else {
                MDC.remove(LoggedAspect.MDC_LOG_KEY);
            }
        }
    }

    /**
     * StackWalker resolution to identify caller class and method when targetClass is omitted.
     */
    private void log(Level level, String logKey, String message, Object[] params, Throwable throwable) {
        Optional<StackFrame> callerFrame = WALKER.walk(frames ->
                frames.filter(frame -> !frame.getClassName().equals(QoopDomainLogger.class.getName()))
                        .findFirst()
        );

        if (callerFrame.isEmpty()) {
            return;
        }

        StackFrame frame = callerFrame.get();
        Class<?> callerClass = frame.getDeclaringClass();
        String methodName = frame.getMethodName();

        String resolvedKey = logKey;
        if (resolvedKey == null) {
            resolvedKey = resolveLoggedKeyFromAnnotation(callerClass, methodName);
        }

        log(level, callerClass, resolvedKey, message, params, throwable);
    }

    /**
     * Helper overload for standard logs without explicit log key or throwable.
     */
    private void log(Level level, String message, Object[] params) {
        log(level, (String) null, message, params, null);
    }

    /**
     * Helper overload for logs with explicit log key.
     */
    private void logWithKey(Level level, String logKey, String message, Object[] params) {
        log(level, logKey, message, params, null);
    }

    /**
     * Helper overload for error logs with exception stack trace.
     */
    private void logError(String logKey, String message, Throwable throwable) {
        log(Level.ERROR, logKey, message, new Object[0], throwable);
    }

    // =========================================================================
    // Reflection Utility
    // =========================================================================

    /**
     * Resolves the log key from method-level or class-level @Logged annotations.
     */
    private String resolveLoggedKeyFromAnnotation(Class<?> clazz, String methodName) {
        if (methodName != null) {
            try {
                for (Method method : clazz.getDeclaredMethods()) {
                    if (method.getName().equals(methodName) && method.isAnnotationPresent(Logged.class)) {
                        return method.getAnnotation(Logged.class).value();
                    }
                }
            } catch (Exception ignored) {
            }
        }

        if (clazz.isAnnotationPresent(Logged.class)) {
            return clazz.getAnnotation(Logged.class).value();
        }

        return "UNKNOWN_KEY";
    }

    private enum Level {INFO, ERROR, WARN, DEBUG}
}