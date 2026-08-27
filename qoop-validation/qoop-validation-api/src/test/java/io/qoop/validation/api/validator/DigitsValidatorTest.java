package io.qoop.validation.api.validator;

import io.qoop.fault.handler.api.exception.DomainValidationException;
import io.qoop.validation.api.Digits;
import io.qoop.validation.api.exception.ValidationExceptionCode;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.lang.annotation.Annotation;
import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

class DigitsValidatorTest {

    private DigitsValidator validator;

    @BeforeEach
    void setUp() {
        validator = new DigitsValidator();
    }

    private Digits createDigits(int integer, int fraction) {
        return new Digits() {
            @Override
            public Class<? extends Annotation> annotationType() {
                return Digits.class;
            }

            @Override
            public int integer() {
                return integer;
            }

            @Override
            public int fraction() {
                return fraction;
            }
        };
    }

    @Test
    @DisplayName("Should pass when value is null")
    void validate_NullValue_Success() {
        Digits annotation = createDigits(3, 2);
        assertDoesNotThrow(() -> validator.validate(null, annotation, "price"));
    }

    @Test
    @DisplayName("Should pass when integer and fraction digits are within bounds")
    void validate_ValidDigits_Success() {
        Digits annotation = createDigits(3, 2);

        assertDoesNotThrow(() -> validator.validate(123, annotation, "price"));
        assertDoesNotThrow(() -> validator.validate(12.34, annotation, "price"));
        assertDoesNotThrow(() -> validator.validate(new BigDecimal("999.99"), annotation, "price"));
        assertDoesNotThrow(() -> validator.validate(-10.5, annotation, "price"));
    }

    @Test
    @DisplayName("Should throw exception when integer digits exceed bounds")
    void validate_ExceedIntegerDigits_ThrowsDomainValidationException() {
        Digits annotation = createDigits(3, 2);

        DomainValidationException exception = assertThrows(
                DomainValidationException.class,
                () -> validator.validate(1234.5, annotation, "price")
        );

        assertEquals(ValidationExceptionCode.INVALID_DIGITS, exception.getCode());
    }

    @Test
    @DisplayName("Should throw exception when fraction digits exceed bounds")
    void validate_ExceedFractionDigits_ThrowsDomainValidationException() {
        Digits annotation = createDigits(3, 2);

        DomainValidationException exception = assertThrows(
                DomainValidationException.class,
                () -> validator.validate(12.345, annotation, "price")
        );

        assertEquals(ValidationExceptionCode.INVALID_DIGITS, exception.getCode());
    }
}