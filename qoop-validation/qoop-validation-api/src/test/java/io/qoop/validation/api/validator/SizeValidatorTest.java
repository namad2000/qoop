package io.qoop.validation.api.validator;

import io.qoop.fault.handler.api.exception.DomainValidationException;
import io.qoop.validation.api.Size;
import io.qoop.validation.api.exception.ValidationExceptionCode;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.lang.annotation.Annotation;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class SizeValidatorTest {

    private SizeValidator validator;

    @BeforeEach
    void setUp() {
        validator = new SizeValidator();
    }

    private Size createSize(int min, int max) {
        return new Size() {
            @Override
            public Class<? extends Annotation> annotationType() {
                return Size.class;
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
        Size annotation = createSize(2, 10);
        assertDoesNotThrow(() -> validator.validate(null, annotation, "items"));
    }

    @Test
    @DisplayName("Should pass when size is within specified bounds")
    void validate_WithinBounds_Success() {
        Size annotation = createSize(2, 5);

        assertDoesNotThrow(() -> validator.validate("abc", annotation, "items"));
        assertDoesNotThrow(() -> validator.validate(List.of("a", "b"), annotation, "items"));
        assertDoesNotThrow(() -> validator.validate(new int[]{1, 2, 3, 4, 5}, annotation, "items"));
    }

    @ParameterizedTest
    @ValueSource(strings = {"a", "abcdef"})
    @DisplayName("Should throw exception when size is out of bounds")
    void validate_OutOfBounds_ThrowsDomainValidationException(String input) {
        Size annotation = createSize(2, 5);

        DomainValidationException exception = assertThrows(
                DomainValidationException.class,
                () -> validator.validate(input, annotation, "items")
        );

        assertEquals(ValidationExceptionCode.INVALID_SIZE, exception.getCode());
    }
}