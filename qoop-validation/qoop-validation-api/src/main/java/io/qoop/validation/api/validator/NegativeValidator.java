package io.qoop.validation.api.validator;

import io.qoop.fault.handler.api.exception.DomainValidationException;
import io.qoop.validation.api.Negative;

import java.math.BigDecimal;
import java.math.BigInteger;

import static io.qoop.validation.api.exception.ValidationExceptionCode.MUST_BE_NEGATIVE;

/**
 * @author Davood Akbari - 1404
 * daak1365@gmail.com
 * daak1365@yahoo.com
 * 09125188694
 */
public class NegativeValidator implements AnnotationValidator<Number, Negative> {

    @Override
    public void validate(Number value, Negative annotation, String paramName) {
        if (value == null) {
            return;
        }

        boolean isNegative = false;

        if (value instanceof BigDecimal) {
            isNegative = ((BigDecimal) value).compareTo(BigDecimal.ZERO) < 0;
        } else if (value instanceof BigInteger) {
            isNegative = ((BigInteger) value).compareTo(BigInteger.ZERO) < 0;
        } else if (value instanceof Byte || value instanceof Short || value instanceof Integer || value instanceof Long) {
            isNegative = value.longValue() < 0L;
        } else if (value instanceof Float || value instanceof Double) {
            isNegative = value.doubleValue() < 0.0;
        }

        if (!isNegative) {
            throw DomainValidationException.withParams(MUST_BE_NEGATIVE, paramName, paramName);
        }
    }
}