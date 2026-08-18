package io.qoop.logs;


import io.qoop.logs.annotation.Logged;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.stereotype.Component;
import tools.jackson.databind.ObjectMapper;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

/**
 * Aspect that intercepts methods or classes annotated with @Logged,
 * serializing input parameters and return objects to JSON strings
 * to prevent default Object.toString() memory address representation.
 */

@Aspect
@Component
public class LoggedAspect {

    public static final String MDC_LOG_KEY = "logKey";

    private final DomainLogger domainLogger;
    private final ObjectMapper objectMapper;

    public LoggedAspect(DomainLogger domainLogger, ObjectMapper objectMapper) {
        this.domainLogger = domainLogger;
        this.objectMapper = objectMapper;
    }

    @Around("@annotation(io.qoop.logs.annotation.Logged) || @within(io.qoop.logs.annotation.Logged)")
    public Object logExecution(ProceedingJoinPoint joinPoint) throws Throwable {
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        Method method = signature.getMethod();
        Class<?> targetClass = joinPoint.getTarget().getClass();

        String logKey = resolveLogKey(method, targetClass);
        Object[] args = joinPoint.getArgs();

        // Convert arguments to readable JSON representations
        String serializedArgs = serializeArguments(args);

        domainLogger.logForClass(
                targetClass,
                logKey,
                "Entering method: {} | Arguments: {}",
                method.getName(),
                serializedArgs
        );

        try {
            Object result = joinPoint.proceed();

            // Convert return object to JSON string
            String serializedResult = serializeObject(result);

            domainLogger.logForClass(
                    targetClass,
                    logKey,
                    "Exiting method: {} | Result: {}",
                    method.getName(),
                    serializedResult
            );

            return result;
        } catch (Throwable throwable) {
            domainLogger.logForClass(
                    targetClass,
                    logKey,
                    "Exception in method: {} | Message: {}",
                    method.getName(),
                    throwable.getMessage()
            );
            throw throwable;
        }
    }

    private String serializeArguments(Object[] args) {
        if (args == null || args.length == 0) {
            return "[]";
        }
        List<String> serializedList = new ArrayList<>();
        for (Object arg : args) {
            serializedList.add(serializeObject(arg));
        }
        return "[" + String.join(", ", serializedList) + "]";
    }

    private String serializeObject(Object obj) {
        if (obj == null) {
            return "null";
        }
        try {
            return objectMapper.writeValueAsString(obj);
        } catch (Exception e) {
            // Fallback if serialization fails or circular dependency occurs
            return obj.toString();
        }
    }

    private String resolveLogKey(Method method, Class<?> targetClass) {
        if (method.isAnnotationPresent(Logged.class)) {
            return method.getAnnotation(Logged.class).value();
        }
        if (targetClass.isAnnotationPresent(Logged.class)) {
            return targetClass.getAnnotation(Logged.class).value();
        }
        return "UNKNOWN_KEY";
    }
}