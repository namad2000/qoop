package io.qoop.validation.api.validator;

import io.qoop.fault.handler.api.exception.DomainValidationException;
import io.qoop.validation.api.DecimalMax;
import io.qoop.validation.api.exception.ValidationExceptionCode;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.lang.annotation.Annotation;
import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

class DecimalMaxValidatorTest {

    private DecimalMaxValidator validator;

    @BeforeEach
    void setUp() {
        validator = new DecimalMaxValidator();
    }

    private DecimalMax createDecimalMax(String value, boolean inclusive) {
        return new DecimalMax() {
            @Override
            public Class<? extends Annotation> annotationType() {
                return DecimalMax.class;
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
        DecimalMax annotation = createDecimalMax("100.50", true);
        assertDoesNotThrow(() -> validator.validate(null, annotation, "discountRate"));
    }

    @Test
    @DisplayName("Should pass when value is less than or equal to maximum (inclusive)")
    void validate_LessThanOrEqualToMaxInclusive_Success() {
        DecimalMax annotation = createDecimalMax("100.50", true);

        assertDoesNotThrow(() -> validator.validate(new BigDecimal("100.50"), annotation, "discountRate"));
        assertDoesNotThrow(() -> validator.validate(50, annotation, "discountRate"));
        assertDoesNotThrow(() -> validator.validate("99.99", annotation, "discountRate"));
    }

    @Test
    @DisplayName("Should pass when value is less than maximum (exclusive)")
    void validate_LessThanMaxExclusive_Success() {
        DecimalMax annotation = createDecimalMax("100.50", false);

        assertDoesNotThrow(() -> validator.validate(new BigDecimal("100.49"), annotation, "discountRate"));
    }

    @Test
    @DisplayName("Should throw exception when value is equal to maximum (exclusive)")
    void validate_EqualToMaxExclusive_ThrowsDomainValidationException() {
        DecimalMax annotation = createDecimalMax("100.50", false);

        DomainValidationException exception = assertThrows(
                DomainValidationException.class,
                () -> validator.validate(new BigDecimal("100.50"), annotation, "discountRate")
        );

        assertEquals(ValidationExceptionCode.VALUE_CANNOT_BE_GREATER_THAN_DECIMAL, exception.getCode());
    }

    @ParameterizedTest
    @ValueSource(strings = {"100.51", "101", "500.0"})
    @DisplayName("Should throw exception when value is greater than maximum")
    void validate_GreaterThanMax_ThrowsDomainValidationException(String input) {
        DecimalMax annotation = createDecimalMax("100.50", true);

        DomainValidationException exception = assertThrows(
                DomainValidationException.class,
                () -> validator.validate(new BigDecimal(input), annotation, "discountRate")
        );

        assertEquals(ValidationExceptionCode.VALUE_CANNOT_BE_GREATER_THAN_DECIMAL, exception.getCode());
    }
}