package io.qoop.logs;

/**
 * Domain logging interface providing methods for standard logging,
 * explicit log key binding, and target class resolution.
 */
public interface DomainLogger {

    void info(String message, Object... params);
    void infoWithKey(String logKey, String message, Object... params);

    void warn(String message, Object... params);
    void warnWithKey(String logKey, String message, Object... params);

    void debug(String message, Object... params);
    void debugWithKey(String logKey, String message, Object... params);

    void error(String message, Throwable throwable);
    void errorWithKey(String logKey, String message, Throwable throwable);

    // =========================================================================
    // Targeted Class Resolution Methods
    // =========================================================================

    void logInfoForClass(Class<?> targetClass, String logKey, String message, Object... params);
    void logWarnForClass(Class<?> targetClass, String logKey, String message, Object... params);
    void logDebugForClass(Class<?> targetClass, String logKey, String message, Object... params);
    void logErrorForClass(Class<?> targetClass, String logKey, String message, Object... params);
    void logErrorForClass(Class<?> targetClass, String logKey, String message, Throwable throwable);
}