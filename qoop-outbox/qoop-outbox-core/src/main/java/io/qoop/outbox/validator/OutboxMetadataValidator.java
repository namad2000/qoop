package io.qoop.outbox.validator;

import io.qoop.outbox.OutBoxEvent;
import org.springframework.aop.support.AopUtils;
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
        Class<?> clazz = AopUtils.getTargetClass(payload);

        OutBoxEvent annotation = clazz.getAnnotation(OutBoxEvent.class);
        if (annotation == null) {
            throw new IllegalArgumentException("Payload class must be annotated with @OutBoxEvent: " + clazz.getName());
        }

        return annotation;
    }
}