package io.qoop.validation.api.validator;

import io.qoop.fault.handler.api.exception.DomainValidationException;
import io.qoop.validation.api.NotNull;

public class NotNullValidator implements AnnotationValidator<Object, NotNull> {

    @Override
    public void validate(Object value, NotNull notNull, String paramName) {
        if (value == null) {
            throw DomainValidationException.of("NotNullValidator-01", paramName);
        }
    }
}
