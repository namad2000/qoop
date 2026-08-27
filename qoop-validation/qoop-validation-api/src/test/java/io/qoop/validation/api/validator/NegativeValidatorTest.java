package io.qoop.validation.api.validator;

import io.qoop.fault.handler.api.exception.DomainValidationException;
import io.qoop.validation.api.Negative;
import io.qoop.validation.api.exception.ValidationExceptionCode;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.lang.annotation.Annotation;
import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

class NegativeValidatorTest {

    private NegativeValidator validator;

    @BeforeEach
    void setUp() {
        validator = new NegativeValidator();
    }

    private Negative createNegative() {
        return new Negative() {
            @Override
            public Class<? extends Annotation> annotationType() {
                return Negative.class;
            }
        };
    }

    @Test
    @DisplayName("Should pass when value is null")
    void validate_NullValue_Success() {
        Negative annotation = createNegative();
        assertDoesNotThrow(() -> validator.validate(null, annotation, "discount"));
    }

    @Test
    @DisplayName("Should pass when value is negative")
    void validate_NegativeValue_Success() {
        Negative annotation = createNegative();

        assertDoesNotThrow(() -> validator.validate(-1, annotation, "discount"));
        assertDoesNotThrow(() -> validator.validate(-10.5, annotation, "discount"));
        assertDoesNotThrow(() -> validator.validate(new BigDecimal("-0.01"), annotation, "discount"));
    }

    @ParameterizedTest
    @ValueSource(doubles = {0.0, 1.0, 10.5})
    @DisplayName("Should throw exception when value is zero or positive")
    void validate_NonNegativeValue_ThrowsDomainValidationException(double input) {
        Negative annotation = createNegative();

        DomainValidationException exception = assertThrows(
                DomainValidationException.class,
                () -> validator.validate(input, annotation, "discount")
        );

        assertEquals(ValidationExceptionCode.MUST_BE_NEGATIVE, exception.getCode());
    }
}