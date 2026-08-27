package io.qoop.validation.api.validator;

import io.qoop.fault.handler.api.exception.DomainValidationException;
import io.qoop.validation.api.PositiveOrZero;

import java.math.BigDecimal;
import java.math.BigInteger;

import static io.qoop.validation.api.exception.ValidationExceptionCode.MUST_BE_POSITIVE_OR_ZERO;

/**
 * @author Davood Akbari - 1404
 * daak1365@gmail.com
 * daak1365@yahoo.com
 * 09125188694
 */
public class PositiveOrZeroValidator implements AnnotationValidator<Number, PositiveOrZero> {

    @Override
    public void validate(Number value, PositiveOrZero annotation, String paramName) {
        if (value == null) {
            return;
        }

        boolean isPositiveOrZero = false;

        if (value instanceof BigDecimal) {
            isPositiveOrZero = ((BigDecimal) value).compareTo(BigDecimal.ZERO) >= 0;
        } else if (value instanceof BigInteger) {
            isPositiveOrZero = ((BigInteger) value).compareTo(BigInteger.ZERO) >= 0;
        } else if (value instanceof Byte || value instanceof Short || value instanceof Integer || value instanceof Long) {
            isPositiveOrZero = value.longValue() >= 0L;
        } else if (value instanceof Float || value instanceof Double) {
            isPositiveOrZero = value.doubleValue() >= 0.0;
        }

        if (!isPositiveOrZero) {
            throw DomainValidationException.withParams(MUST_BE_POSITIVE_OR_ZERO, paramName, paramName);
        }
    }
}