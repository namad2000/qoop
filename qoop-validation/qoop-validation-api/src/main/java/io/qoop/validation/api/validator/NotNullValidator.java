package io.qoop.validation.api.validator;

import io.qoop.fault.handler.api.exception.DomainValidationException;
import io.qoop.validation.api.NotNull;

import static io.qoop.validation.api.exception.ValidationExceptionCode.NOT_NULL;

/**
 * @author Davood Akbari - 1404
 * daak1365@gmail.com
 * daak1365@yahoo.com
 * 09125188694
 */

public class NotNullValidator implements AnnotationValidator<Object, NotNull> {

    @Override
    public void validate(Object value, NotNull notNull, String paramName) {
        if (value == null) {
            throw DomainValidationException.withParams(NOT_NULL, paramName, paramName);
        }
    }
}
