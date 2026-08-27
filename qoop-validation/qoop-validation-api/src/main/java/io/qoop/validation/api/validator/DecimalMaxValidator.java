package io.qoop.validation.api.validator;

import io.qoop.fault.handler.api.exception.DomainValidationException;
import io.qoop.validation.api.DecimalMax;

import java.math.BigDecimal;
import java.math.BigInteger;

import static io.qoop.validation.api.exception.ValidationExceptionCode.VALUE_CANNOT_BE_GREATER_THAN_DECIMAL;

/**
 * @author Davood Akbari - 1404
 * daak1365@gmail.com
 * daak1365@yahoo.com
 * 09125188694
 */
public class DecimalMaxValidator implements AnnotationValidator<Object, DecimalMax> {

    @Override
    public void validate(Object value, DecimalMax annotation, String paramName) {
        if (value == null) {
            return;
        }

        BigDecimal numValue;
        if (value instanceof BigDecimal) {
            numValue = (BigDecimal) value;
        } else if (value instanceof BigInteger) {
            numValue = new BigDecimal((BigInteger) value);
        } else if (value instanceof Byte || value instanceof Short || value instanceof Integer || value instanceof Long) {
            numValue = BigDecimal.valueOf(((Number) value).longValue());
        } else if (value instanceof Float || value instanceof Double) {
            numValue = BigDecimal.valueOf(((Number) value).doubleValue());
        } else if (value instanceof String) {
            try {
                numValue = new BigDecimal((String) value);
            } catch (NumberFormatException e) {
                throw DomainValidationException.withParams(VALUE_CANNOT_BE_GREATER_THAN_DECIMAL, paramName, paramName, annotation.value());
            }
        } else {
            throw DomainValidationException.withParams(VALUE_CANNOT_BE_GREATER_THAN_DECIMAL, paramName, paramName, annotation.value());
        }

        BigDecimal maxValue = new BigDecimal(annotation.value());
        int comparison = numValue.compareTo(maxValue);

        boolean isValid = annotation.inclusive() ? comparison <= 0 : comparison < 0;

        if (!isValid) {
            throw DomainValidationException.withParams(VALUE_CANNOT_BE_GREATER_THAN_DECIMAL, paramName, paramName, annotation.value());
        }
    }
}