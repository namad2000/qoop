package io.qoop.outbox;

import org.springframework.stereotype.Component;

import java.lang.reflect.Field;

/**
 * Component responsible for extracting the aggregate identifier from payload objects
 * using the @AggregateId annotation.
 */
@Component
public class AggregateIdExtractor {

    public String extract(Object payload) {
        if (payload == null) {
            throw new IllegalArgumentException("Payload cannot be null for aggregate ID extraction.");
        }

        Class<?> clazz = payload.getClass();
        for (Field field : clazz.getDeclaredFields()) {
            if (field.isAnnotationPresent(AggregateId.class)) {
                field.setAccessible(true);
                try {
                    Object value = field.get(payload);
                    return value != null ? value.toString() : null;
                } catch (IllegalAccessException e) {
                    throw new RuntimeException("Failed to access @AggregateId field value", e);
                }
            }
        }
        throw new IllegalArgumentException("No field annotated with @AggregateId found in class: " + clazz.getName());
    }
}