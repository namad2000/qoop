package io.qoop.outbox.validator;

import io.qoop.outbox.OutBoxEvent;
import org.springframework.stereotype.Component;

/**
 * Validator component to ensure payload classes meet outbox requirements.
 */
@Component
public class OutboxMetadataValidator {

    public OutBoxEvent validateAndGetAnnotation(Object payload) {
        if (payload == null) {
            throw new IllegalArgumentException("Payload cannot be null.");
        }

        Class<?> clazz = payload.getClass();
        if (!clazz.isAnnotationPresent(OutBoxEvent.class)) {
            throw new IllegalArgumentException("Payload class must be annotated with @OutBoxEvent: " + clazz.getName());

        }

        return clazz.getAnnotation(OutBoxEvent.class);
    }
}