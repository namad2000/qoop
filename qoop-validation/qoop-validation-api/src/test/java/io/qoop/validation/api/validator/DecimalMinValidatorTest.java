package io.qoop.validation.api.validator;

import io.qoop.fault.handler.api.exception.DomainValidationException;
import io.qoop.validation.api.DecimalMin;
import io.qoop.validation.api.exception.ValidationExceptionCode;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.lang.annotation.Annotation;
import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

class DecimalMinValidatorTest {

    private DecimalMinValidator validator;

    @BeforeEach
    void setUp() {
        validator = new DecimalMinValidator();
    }

    private DecimalMin createDecimalMin(String value, boolean inclusive) {
        return new DecimalMin() {
            @Override
            public Class<? extends Annotation> annotationType() {
                return DecimalMin.class;
            }

            @Override
            public String value() {
                return value;
            }

            @Override
            public boolean inclusive() {
                return inclusive;
            }
        };
    }

    @Test
    @DisplayName("Should pass when value is null")
    void validate_NullValue_Success() {
        DecimalMin annotation = createDecimalMin("10.50", true);
        assertDoesNotThrow(() -> validator.validate(null, annotation, "amount"));
    }

    @Test
    @DisplayName("Should pass when value is greater than or equal to minimum (inclusive)")
    void validate_GreaterThanOrEqualToMinInclusive_Success() {
        DecimalMin annotation = createDecimalMin("10.50", true);

        assertDoesNotThrow(() -> validator.validate(new BigDecimal("10.50"), annotation, "amount"));
        assertDoesNotThrow(() -> validator.validate(11, annotation, "amount"));
        assertDoesNotThrow(() -> validator.validate("15.75", annotation, "amount"));
    }

    @Test
    @DisplayName("Should pass when value is greater than minimum (exclusive)")
    void validate_GreaterThanMinExclusive_Success() {
        DecimalMin annotation = createDecimalMin("10.50", false);

        assertDoesNotThrow(() -> validator.validate(new BigDecimal("10.51"), annotation, "amount"));
    }

    @Test
    @DisplayName("Should throw exception when value is equal to minimum (exclusive)")
    void validate_EqualToMinExclusive_ThrowsDomainValidationException() {
        DecimalMin annotation = createDecimalMin("10.50", false);

        DomainValidationException exception = assertThrows(
                DomainValidationException.class,
                () -> validator.validate(new BigDecimal("10.50"), annotation, "amount")
        );

        assertEquals(ValidationExceptionCode.VALUE_CANNOT_BE_LESS_THAN_DECIMAL, exception.getCode());
    }

    @ParameterizedTest
    @ValueSource(strings = {"10.49", "0", "-5.0"})
    @DisplayName("Should throw exception when value is less than minimum")
    void validate_LessThanMin_ThrowsDomainValidationException(String input) {
        DecimalMin annotation = createDecimalMin("10.50", true);

        DomainValidationException exception = assertThrows(
                DomainValidationException.class,
                () -> validator.validate(new BigDecimal(input), annotation, "amount")
        );

        assertEquals(ValidationExceptionCode.VALUE_CANNOT_BE_LESS_THAN_DECIMAL, exception.getCode());
    }
}