package io.qoop.validation.api.validator;

import io.qoop.fault.handler.api.exception.DomainValidationException;
import io.qoop.validation.api.Range;

import java.math.BigDecimal;
import java.math.BigInteger;

import static io.qoop.validation.api.exception.ValidationExceptionCode.INVALID_RANGE;

/**
 * @author Davood Akbari - 1404
 * daak1365@gmail.com
 * daak1365@yahoo.com
 * 09125188694
 */
public class RangeValidator implements AnnotationValidator<Object, Range> {

    @Override
    public void validate(Object value, Range annotation, String paramName) {
        if (value == null) {
            return;
        }

        BigDecimal numValue = obtainBigDecimal(value);
        if (numValue == null) {
            return;
        }

        BigDecimal min = BigDecimal.valueOf(annotation.min());
        BigDecimal max = BigDecimal.valueOf(annotation.max());

        if (numValue.compareTo(min) < 0 || numValue.compareTo(max) > 0) {
            throw DomainValidationException.withParams(INVALID_RANGE, paramName, paramName, annotation.min(), annotation.max());
        }
    }

    private BigDecimal obtainBigDecimal(Object value) {
        if (value instanceof BigDecimal) {
            return (BigDecimal) value;
        }
        if (value instanceof BigInteger) {
            return new BigDecimal((BigInteger) value);
        }
        if (value instanceof Number) {
            return BigDecimal.valueOf(((Number) value).doubleValue());
        }
        if (value instanceof CharSequence) {
            try {
                return new BigDecimal(value.toString().trim());
            } catch (NumberFormatException ignored) {
                return null;
            }
        }
        return null;
    }
}