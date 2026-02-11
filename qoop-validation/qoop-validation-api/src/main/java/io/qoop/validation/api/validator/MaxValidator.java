package io.qoop.validation.api.validator;

import io.qoop.fault.handler.api.exception.DomainValidationException;
import io.qoop.validation.api.Max;

import static io.qoop.validation.api.exception.ValidationExceptionCode.VALUE_CANNOT_BE_GREATER_THAN;

/**
 * @author Davood Akbari - 1404
 * daak1365@gmail.com
 * daak1365@yahoo.com
 * 09125188694
 */

public class MaxValidator implements AnnotationValidator<Number, Max> {
    @Override
    public void validate(Number value, Max max, String paramName) {
        if (value != null && value.longValue() > max.value()) {
            throw DomainValidationException.withParams(VALUE_CANNOT_BE_GREATER_THAN, paramName, max.value());
        }
    }
}
