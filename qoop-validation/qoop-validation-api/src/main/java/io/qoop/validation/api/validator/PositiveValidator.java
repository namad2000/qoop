package io.qoop.validation.api.validator;

import io.qoop.fault.handler.api.exception.DomainValidationException;
import io.qoop.validation.api.Positive;

import java.math.BigDecimal;
import java.math.BigInteger;

import static io.qoop.validation.api.exception.ValidationExceptionCode.MUST_BE_POSITIVE;

/**
 * @author Davood Akbari - 1404
 * daak1365@gmail.com
 * daak1365@yahoo.com
 * 09125188694
 */
public class PositiveValidator implements AnnotationValidator<Number, Positive> {

    @Override
    public void validate(Number value, Positive annotation, String paramName) {
        if (value == null) {
            return;
        }

        boolean isPositive = false;

        if (value instanceof BigDecimal) {
            isPositive = ((BigDecimal) value).compareTo(BigDecimal.ZERO) > 0;
        } else if (value instanceof BigInteger) {
            isPositive = ((BigInteger) value).compareTo(BigInteger.ZERO) > 0;
        } else if (value instanceof Byte || value instanceof Short || value instanceof Integer || value instanceof Long) {
            isPositive = value.longValue() > 0L;
        } else if (value instanceof Float || value instanceof Double) {
            isPositive = value.doubleValue() > 0.0;
        }

        if (!isPositive) {
            throw DomainValidationException.withParams(MUST_BE_POSITIVE, paramName, paramName);
        }
    }
}