package io.qoop.validation.api.validator;

import io.qoop.fault.handler.api.exception.DomainValidationException;
import io.qoop.validation.api.PositiveOrZero;
import io.qoop.validation.api.exception.ValidationExceptionCode;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.lang.annotation.Annotation;
import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

class PositiveOrZeroValidatorTest {

    private PositiveOrZeroValidator validator;

    @BeforeEach
    void setUp() {
        validator = new PositiveOrZeroValidator();
    }

    private PositiveOrZero createPositiveOrZero() {
        return new PositiveOrZero() {
            @Override
            public Class<? extends Annotation> annotationType() {
                return PositiveOrZero.class;
            }
        };
    }

    @Test
    @DisplayName("Should pass when value is null")
    void validate_NullValue_Success() {
        PositiveOrZero annotation = createPositiveOrZero();
        assertDoesNotThrow(() -> validator.validate(null, annotation, "balance"));
    }

    @Test
    @DisplayName("Should pass when value is positive or zero")
    void validate_PositiveOrZeroValue_Success() {
        PositiveOrZero annotation = createPositiveOrZero();

        assertDoesNotThrow(() -> validator.validate(0, annotation, "balance"));
        assertDoesNotThrow(() -> validator.validate(10, annotation, "balance"));
        assertDoesNotThrow(() -> validator.validate(0.0, annotation, "balance"));
        assertDoesNotThrow(() -> validator.validate(BigDecimal.ZERO, annotation, "balance"));
        assertDoesNotThrow(() -> validator.validate(new BigDecimal("15.5"), annotation, "balance"));
    }

    @ParameterizedTest
    @ValueSource(doubles = {-1.0, -0.01, -100.5})
    @DisplayName("Should throw exception when value is negative")
    void validate_NegativeValue_ThrowsDomainValidationException(double input) {
        PositiveOrZero annotation = createPositiveOrZero();

        DomainValidationException exception = assertThrows(
                DomainValidationException.class,
                () -> validator.validate(input, annotation, "balance")
        );

        assertEquals(ValidationExceptionCode.MUST_BE_POSITIVE_OR_ZERO, exception.getCode());
    }
}