package io.qoop.validation.api.validator;

import io.qoop.fault.handler.api.exception.DomainValidationException;
import io.qoop.validation.api.Numeric;
import io.qoop.validation.api.exception.ValidationExceptionCode;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.lang.annotation.Annotation;

import static org.junit.jupiter.api.Assertions.*;

class NumericValidatorTest {

    private NumericValidator validator;

    @BeforeEach
    void setUp() {
        validator = new NumericValidator();
    }

    private Numeric createNumeric() {
        return new Numeric() {
            @Override
            public Class<? extends Annotation> annotationType() {
                return Numeric.class;
            }
        };
    }

    @Test
    @DisplayName("Should pass when value is null or empty")
    void validate_NullOrEmptyValue_Success() {
        Numeric annotation = createNumeric();
        assertDoesNotThrow(() -> validator.validate(null, annotation, "code"));
        assertDoesNotThrow(() -> validator.validate("", annotation, "code"));
    }

    @ParameterizedTest
    @ValueSource(strings = {"123", "0", "009821"})
    @DisplayName("Should pass when input consists of digits only")
    void validate_ValidNumericString_Success(String input) {
        Numeric annotation = createNumeric();
        assertDoesNotThrow(() -> validator.validate(input, annotation, "code"));
    }

    @ParameterizedTest
    @ValueSource(strings = {"123a", "12.3", "-123", "12 3", "abc"})
    @DisplayName("Should throw exception when input contains non-digit characters")
    void validate_NonNumericString_ThrowsDomainValidationException(String input) {
        Numeric annotation = createNumeric();

        DomainValidationException exception = assertThrows(
                DomainValidationException.class,
                () -> validator.validate(input, annotation, "code")
        );

        assertEquals(ValidationExceptionCode.MUST_BE_NUMERIC, exception.getCode());
    }
}