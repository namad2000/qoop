package io.qoop.validation.api.validator;

import io.qoop.fault.handler.api.exception.DomainValidationException;
import io.qoop.validation.api.Pattern;
import io.qoop.validation.api.exception.ValidationExceptionCode;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.lang.annotation.Annotation;

import static org.junit.jupiter.api.Assertions.*;

class PatternValidatorTest {

    private PatternValidator validator;

    @BeforeEach
    void setUp() {
        validator = new PatternValidator();
    }

    private Pattern createPattern(String regex) {
        return new Pattern() {
            @Override
            public Class<? extends Annotation> annotationType() {
                return Pattern.class;
            }

            @Override
            public String regexp() {
                return regex;
            }

            @Override
            public int flags() {
                return 0;
            }
        };
    }

    @Test
    @DisplayName("Should pass when value is null or empty")
    void validate_NullOrEmptyValue_Success() {
        Pattern annotation = createPattern("^[a-z]+$");
        assertDoesNotThrow(() -> validator.validate(null, annotation, "username"));
        assertDoesNotThrow(() -> validator.validate("", annotation, "username"));
    }

    @ParameterizedTest
    @ValueSource(strings = {"abc", "qoop", "test"})
    @DisplayName("Should pass when value matches pattern")
    void validate_ValidPattern_Success(String input) {
        Pattern annotation = createPattern("^[a-z]+$");
        assertDoesNotThrow(() -> validator.validate(input, annotation, "username"));
    }

    @ParameterizedTest
    @ValueSource(strings = {"ABC", "qoop123", "test_123"})
    @DisplayName("Should throw exception when value does not match pattern")
    void validate_InvalidPattern_ThrowsDomainValidationException(String input) {
        Pattern annotation = createPattern("^[a-z]+$");

        DomainValidationException exception = assertThrows(
                DomainValidationException.class,
                () -> validator.validate(input, annotation, "username")
        );

        assertEquals(ValidationExceptionCode.INVALID_PATTERN, exception.getCode());
    }
}