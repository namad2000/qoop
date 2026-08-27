package io.qoop.validation.api.validator;

import io.qoop.fault.handler.api.exception.DomainValidationException;
import io.qoop.validation.api.NotEmpty;

import java.lang.reflect.Array;
import java.util.Collection;
import java.util.Map;

import static io.qoop.validation.api.exception.ValidationExceptionCode.NOT_EMPTY;

/**
 * @author Davood Akbari - 1404
 * daak1365@gmail.com
 * daak1365@yahoo.com
 * 09125188694
 */

public class NotEmptyValidator implements AnnotationValidator<Object, NotEmpty> {

    @Override
    public void validate(Object value, NotEmpty annotation, String paramName) {
        if (value == null) {
            throw DomainValidationException.withParams(NOT_EMPTY, paramName, paramName);
        }

        boolean isEmpty;

        if (value instanceof String) {
            isEmpty = ((String) value).trim().isEmpty();
        } else if (value instanceof CharSequence) {
            isEmpty = ((CharSequence) value).isEmpty();
        } else if (value instanceof Collection<?>) {
            isEmpty = ((Collection<?>) value).isEmpty();
        } else if (value instanceof Map<?, ?>) {
            isEmpty = ((Map<?, ?>) value).isEmpty();
        } else if (value.getClass().isArray()) {
            isEmpty = Array.getLength(value) == 0;
        } else {
            throw DomainValidationException.withParams(NOT_EMPTY, paramName, paramName);
        }

        if (isEmpty) {
            throw DomainValidationException.withParams(NOT_EMPTY, paramName, paramName);
        }
    }
}