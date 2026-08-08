package io.qoop.validation.api.validator;


import io.qoop.fault.handler.api.exception.DomainValidationException;
import io.qoop.validation.api.Min;

import static io.qoop.validation.api.exception.ValidationExceptionCode.VALUE_CANNOT_BE_LESS_THAN;

public class MinValidator implements AnnotationValidator<Number, Min> {

    @Override
    public void validate(Number value, Min min, String paramName) {
        if (value != null && value.longValue() < min.value()) {
            throw DomainValidationException.withParams(VALUE_CANNOT_BE_LESS_THAN, paramName, paramName, min.value());
        }
    }
}
