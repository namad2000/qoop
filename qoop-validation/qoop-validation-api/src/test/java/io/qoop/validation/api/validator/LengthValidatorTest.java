package io.qoop.validation.api.validator;

import io.qoop.fault.handler.api.exception.DomainValidationException;
import io.qoop.validation.api.Length;
import io.qoop.validation.api.exception.ValidationExceptionCode;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.lang.annotation.Annotation;

import static org.junit.jupiter.api.Assertions.*;

class LengthValidatorTest {

    private LengthValidator validator;

    @BeforeEach
    void setUp() {
        validator = new LengthValidator();
    }

    private Length createLength(int min, int max) {
        return new Length() {
            @Override
            public Class<? extends Annotation> annotationType() {
                return Length.class;
            }

            @Override
            public int min() {
                return min;
            }

            @Override
            public int max() {
                return max;
            }
        };
    }

    @Test
    @DisplayName("Should pass when value is null")
    void validate_NullValue_Success() {
        Length annotation = createLength(3, 10);
        assertDoesNotThrow(() -> validator.validate(null, annotation, "username"));
    }

    @Test
    @DisplayName("Should pass when string length is within range")
    void validate_ValidLength_Success() {
        Length annotation = createLength(3, 10);
        assertDoesNotThrow(() -> validator.validate("qoopUser", annotation, "username"));
    }

    @ParameterizedTest
    @ValueSource(strings = {"ab", "exceedsMaximumAllowedLengthValue"})
    @DisplayName("Should throw exception when string length is out of range")
    void validate_InvalidLength_ThrowsDomainValidationException(String input) {
        Length annotation = createLength(3, 10);

        DomainValidationException exception = assertThrows(
                DomainValidationException.class,
                () -> validator.validate(input, annotation, "username")
        );

        assertEquals(ValidationExceptionCode.INVALID_LENGTH, exception.getCode());
    }
}