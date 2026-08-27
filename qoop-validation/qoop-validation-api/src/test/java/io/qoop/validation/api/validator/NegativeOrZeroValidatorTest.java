package io.qoop.validation.api.validator;

import io.qoop.fault.handler.api.exception.DomainValidationException;
import io.qoop.validation.api.NegativeOrZero;
import io.qoop.validation.api.exception.ValidationExceptionCode;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.lang.annotation.Annotation;
import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

class NegativeOrZeroValidatorTest {

    private NegativeOrZeroValidator validator;

    @BeforeEach
    void setUp() {
        validator = new NegativeOrZeroValidator();
    }

    private NegativeOrZero createNegativeOrZero() {
        return new NegativeOrZero() {
            @Override
            public Class<? extends Annotation> annotationType() {
                return NegativeOrZero.class;
            }
        };
    }

    @Test
    @DisplayName("Should pass when value is null")
    void validate_NullValue_Success() {
        NegativeOrZero annotation = createNegativeOrZero();
        assertDoesNotThrow(() -> validator.validate(null, annotation, "penalty"));
    }

    @Test
    @DisplayName("Should pass when value is negative or zero")
    void validate_NegativeOrZeroValue_Success() {
        NegativeOrZero annotation = createNegativeOrZero();

        assertDoesNotThrow(() -> validator.validate(0, annotation, "penalty"));
        assertDoesNotThrow(() -> validator.validate(-10, annotation, "penalty"));
        assertDoesNotThrow(() -> validator.validate(0.0, annotation, "penalty"));
        assertDoesNotThrow(() -> validator.validate(BigDecimal.ZERO, annotation, "penalty"));
        assertDoesNotThrow(() -> validator.validate(new BigDecimal("-15.5"), annotation, "penalty"));
    }

    @ParameterizedTest
    @ValueSource(doubles = {1.0, 0.01, 100.5})
    @DisplayName("Should throw exception when value is positive")
    void validate_PositiveValue_ThrowsDomainValidationException(double input) {
        NegativeOrZero annotation = createNegativeOrZero();

        DomainValidationException exception = assertThrows(
                DomainValidationException.class,
                () -> validator.validate(input, annotation, "penalty")
        );

        assertEquals(ValidationExceptionCode.MUST_BE_NEGATIVE_OR_ZERO, exception.getCode());
    }
}