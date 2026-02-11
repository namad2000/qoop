package io.qoop.validation.api.validator;


import io.qoop.fault.handler.api.exception.DomainValidationException;
import io.qoop.validation.api.Min;

public class MinValidator implements AnnotationValidator<Number, Min> {
    @Override
    public void validate(Number value, Min min, String paramName) {
        if (value != null && value.longValue() < min.value()) {
            throw DomainValidationException.withParams("MinValidator-01", paramName, min.value());
        }
    }
}
