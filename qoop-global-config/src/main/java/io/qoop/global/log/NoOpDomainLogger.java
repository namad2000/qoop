package io.qoop.global.log;

import io.qoop.logs.DomainLogger;

/**
 * Null Object pattern implementation for {@link DomainLogger}.
 * Performs no operations when methods are invoked. Useful for testing,
 * fallback scenarios, or when logging is explicitly disabled.
 */
public class NoOpDomainLogger implements DomainLogger {

    public static final DomainLogger INSTANCE = new NoOpDomainLogger();

    @Override
    public void info(String message, Object... params) {
        // Intentional No-Op
    }

    @Override
    public void infoWithKey(String logKey, String message, Object... params) {
        // Intentional No-Op
    }

    @Override
    public void warn(String message, Object... params) {
        // Intentional No-Op
    }

    @Override
    public void warnWithKey(String logKey, String message, Object... params) {
        // Intentional No-Op
    }

    @Override
    public void debug(String message, Object... params) {
        // Intentional No-Op
    }

    @Override
    public void debugWithKey(String logKey, String message, Object... params) {
        // Intentional No-Op
    }

    @Override
    public void error(String message, Throwable throwable) {
        // Intentional No-Op
    }

    @Override
    public void errorWithKey(String logKey, String message, Throwable throwable) {
        // Intentional No-Op
    }

    @Override
    public void logInfoForClass(Class<?> targetClass, String logKey, String message, Object... params) {
        // Intentional No-Op
    }

    @Override
    public void logWarnForClass(Class<?> targetClass, String logKey, String message, Object... params) {
        // Intentional No-Op
    }

    @Override
    public void logDebugForClass(Class<?> targetClass, String logKey, String message, Object... params) {
        // Intentional No-Op
    }

    @Override
    public void logErrorForClass(Class<?> targetClass, String logKey, String message, Object... params) {
        // Intentional No-Op
    }

    @Override
    public void logErrorForClass(Class<?> targetClass, String logKey, String message, Throwable throwable) {
        // Intentional No-Op
    }
}