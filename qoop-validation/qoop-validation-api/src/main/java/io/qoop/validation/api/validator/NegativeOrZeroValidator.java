package io.qoop.validation.api.validator;

import io.qoop.fault.handler.api.exception.DomainValidationException;
import io.qoop.validation.api.NegativeOrZero;

import java.math.BigDecimal;
import java.math.BigInteger;

import static io.qoop.validation.api.exception.ValidationExceptionCode.MUST_BE_NEGATIVE_OR_ZERO;

/**
 * @author Davood Akbari - 1404
 * daak1365@gmail.com
 * daak1365@yahoo.com
 * 09125188694
 */
public class NegativeOrZeroValidator implements AnnotationValidator<Number, NegativeOrZero> {

    @Override
    public void validate(Number value, NegativeOrZero annotation, String paramName) {
        if (value == null) {
            return;
        }

        boolean isNegativeOrZero = false;

        if (value instanceof BigDecimal) {
            isNegativeOrZero = ((BigDecimal) value).compareTo(BigDecimal.ZERO) <= 0;
        } else if (value instanceof BigInteger) {
            isNegativeOrZero = ((BigInteger) value).compareTo(BigInteger.ZERO) <= 0;
        } else if (value instanceof Byte || value instanceof Short || value instanceof Integer || value instanceof Long) {
            isNegativeOrZero = value.longValue() <= 0L;
        } else if (value instanceof Float || value instanceof Double) {
            isNegativeOrZero = value.doubleValue() <= 0.0;
        }

        if (!isNegativeOrZero) {
            throw DomainValidationException.withParams(MUST_BE_NEGATIVE_OR_ZERO, paramName, paramName);
        }
    }
}