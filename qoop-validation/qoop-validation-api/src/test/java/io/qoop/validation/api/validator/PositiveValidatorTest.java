package io.qoop.validation.api.validator;

import io.qoop.fault.handler.api.exception.DomainValidationException;
import io.qoop.validation.api.Positive;
import io.qoop.validation.api.exception.ValidationExceptionCode;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.lang.annotation.Annotation;
import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

class PositiveValidatorTest {

    private PositiveValidator validator;

    @BeforeEach
    void setUp() {
        validator = new PositiveValidator();
    }

    private Positive createPositive() {
        return new Positive() {
            @Override
            public Class<? extends Annotation> annotationType() {
                return Positive.class;
            }
        };
    }

    @Test
    @DisplayName("Should pass when value is null")
    void validate_NullValue_Success() {
        Positive annotation = createPositive();
        assertDoesNotThrow(() -> validator.validate(null, annotation, "amount"));
    }

    @Test
    @DisplayName("Should pass when value is positive")
    void validate_PositiveValue_Success() {
        Positive annotation = createPositive();

        assertDoesNotThrow(() -> validator.validate(1, annotation, "amount"));
        assertDoesNotThrow(() -> validator.validate(10.5, annotation, "amount"));
        assertDoesNotThrow(() -> validator.validate(new BigDecimal("0.01"), annotation, "amount"));
    }

    @ParameterizedTest
    @ValueSource(doubles = {0.0, -1.0, -10.5})
    @DisplayName("Should throw exception when value is zero or negative")
    void validate_NonPositiveValue_ThrowsDomainValidationException(double input) {
        Positive annotation = createPositive();

        DomainValidationException exception = assertThrows(
                DomainValidationException.class,
                () -> validator.validate(input, annotation, "amount")
        );

        assertEquals(ValidationExceptionCode.MUST_BE_POSITIVE, exception.getCode());
    }
}