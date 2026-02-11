package io.qoop.validation.api.validator;

import io.qoop.fault.handler.api.exception.DomainValidationException;
import io.qoop.validation.api.NotEmpty;

public class NotEmptyValidator implements AnnotationValidator<String, NotEmpty> {

    @Override
    public void validate(String value, NotEmpty NotEmpty, String paramName) {
        if (value == null || value.trim().isEmpty()) {
            throw DomainValidationException.of("NotEmptyValidator-01", paramName);
        }
    }
}
