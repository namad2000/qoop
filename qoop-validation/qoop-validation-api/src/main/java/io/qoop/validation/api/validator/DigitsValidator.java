package io.qoop.validation.api.validator;

import io.qoop.fault.handler.api.exception.DomainValidationException;
import io.qoop.validation.api.Digits;

import java.math.BigDecimal;
import java.math.BigInteger;

import static io.qoop.validation.api.exception.ValidationExceptionCode.INVALID_DIGITS;

/**
 * @author Davood Akbari - 1404
 * daak1365@gmail.com
 * daak1365@yahoo.com
 * 09125188694
 */
public class DigitsValidator implements AnnotationValidator<Number, Digits> {

    @Override
    public void validate(Number value, Digits annotation, String paramName) {
        if (value == null) {
            return;
        }

        BigDecimal bigDecimalValue;
        if (value instanceof BigDecimal) {
            bigDecimalValue = (BigDecimal) value;
        } else if (value instanceof BigInteger) {
            bigDecimalValue = new BigDecimal((BigInteger) value);
        } else if (value instanceof Byte || value instanceof Short || value instanceof Integer || value instanceof Long) {
            bigDecimalValue = BigDecimal.valueOf(value.longValue());
        } else {
            bigDecimalValue = new BigDecimal(value.toString());
        }

        BigDecimal normalizedValue = bigDecimalValue.stripTrailingZeros();
        int integerPartLength = normalizedValue.precision() - normalizedValue.scale();
        if (integerPartLength < 0) {
            integerPartLength = 1;
        }
        
        int fractionPartLength = Math.max(0, normalizedValue.scale());

        if (integerPartLength > annotation.integer() || fractionPartLength > annotation.fraction()) {
            throw DomainValidationException.withParams(
                    INVALID_DIGITS,
                    paramName,
                    paramName,
                    annotation.integer(),
                    annotation.fraction()
            );
        }
    }
}