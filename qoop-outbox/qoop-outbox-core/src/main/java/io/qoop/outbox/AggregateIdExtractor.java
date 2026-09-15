package io.qoop.outbox;

import org.springframework.aop.support.AopUtils;
import org.springframework.stereotype.Component;
import org.springframework.util.ReflectionUtils;

import java.lang.reflect.Field;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Component responsible for extracting the aggregate identifier from payload objects
 * using the @AggregateId annotation.
 */
@Component
public class AggregateIdExtractor {

    private final Map<Class<?>, Field> fieldCache = new ConcurrentHashMap<>();

    public String extract(Object payload) {
        if (payload == null) {
            throw new IllegalArgumentException("Payload cannot be null for aggregate ID extraction.");
        }

        Class<?> targetClass = AopUtils.getTargetClass(payload);

        Field field = fieldCache.computeIfAbsent(targetClass, this::findAggregateIdField);

        try {
            Object value = field.get(payload);
            return value != null ? value.toString() : null;
        } catch (IllegalAccessException e) {
            throw new RuntimeException("Failed to access @AggregateId field value in class: " + targetClass.getName(), e);
        }
    }

    private Field findAggregateIdField(Class<?> clazz) {
        Class<?> current = clazz;
        while (current != null && current != Object.class) {
            for (Field field : current.getDeclaredFields()) {
                if (field.isAnnotationPresent(AggregateId.class)) {
                    ReflectionUtils.makeAccessible(field);
                    return field;
                }
            }
            current = current.getSuperclass();
        }

        throw new IllegalArgumentException("No field annotated with @AggregateId found in class: " + clazz.getName());
    }
}