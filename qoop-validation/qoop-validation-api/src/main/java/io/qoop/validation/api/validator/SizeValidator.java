package io.qoop.validation.api.validator;

import io.qoop.fault.handler.api.exception.DomainValidationException;
import io.qoop.validation.api.Size;

import java.lang.reflect.Array;
import java.util.Collection;
import java.util.Map;

import static io.qoop.validation.api.exception.ValidationExceptionCode.INVALID_SIZE;

/**
 * @author Davood Akbari - 1404
 * daak1365@gmail.com
 * daak1365@yahoo.com
 * 09125188694
 */
public class SizeValidator implements AnnotationValidator<Object, Size> {

    @Override
    public void validate(Object value, Size annotation, String paramName) {
        if (value == null) {
            return;
        }

        int length;
        if (value instanceof CharSequence) {
            length = ((CharSequence) value).length();
        } else if (value instanceof Collection<?>) {
            length = ((Collection<?>) value).size();
        } else if (value instanceof Map<?, ?>) {
            length = ((Map<?, ?>) value).size();
        } else if (value.getClass().isArray()) {
            length = Array.getLength(value);
        } else {
            throw DomainValidationException.withParams(INVALID_SIZE, paramName, paramName, annotation.min(), annotation.max());
        }

        if (length < annotation.min() || length > annotation.max()) {
            throw DomainValidationException.withParams(INVALID_SIZE, paramName, paramName, annotation.min(), annotation.max());
        }
    }
}