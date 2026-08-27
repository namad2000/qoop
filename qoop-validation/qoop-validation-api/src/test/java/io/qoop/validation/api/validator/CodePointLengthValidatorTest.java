package io.qoop.validation.api.validator;

import io.qoop.fault.handler.api.exception.DomainValidationException;
import io.qoop.validation.api.CodePointLength;
import io.qoop.validation.api.exception.ValidationExceptionCode;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.lang.annotation.Annotation;

import static org.junit.jupiter.api.Assertions.*;

class CodePointLengthValidatorTest {

    private CodePointLengthValidator validator;

    @BeforeEach
    void setUp() {
        validator = new CodePointLengthValidator();
    }

    private CodePointLength createCodePointLength(int min, int max) {
        return new CodePointLength() {
            @Override
            public Class<? extends Annotation> annotationType() {
                return CodePointLength.class;
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
        CodePointLength annotation = createCodePointLength(2, 5);
        assertDoesNotThrow(() -> validator.validate(null, annotation, "text"));
    }

    @Test
    @DisplayName("Should pass when code point length is within bounds (including Unicode surrogate pairs)")
    void validate_WithinBounds_Success() {
        CodePointLength annotation = createCodePointLength(2, 5);

        // Standard string with 3 characters
        assertDoesNotThrow(() -> validator.validate("abc", annotation, "text"));

        // Unicode emoji (Unicode code point representation)
        // 😀 (U+1F600) is 2 char units in String.length() but 1 code point
        assertDoesNotThrow(() -> validator.validate("😀😀", annotation, "text"));
    }

    @Test
    @DisplayName("Should throw exception when code point length is out of bounds")
    void validate_OutOfBounds_ThrowsDomainValidationException() {
        CodePointLength annotation = createCodePointLength(2, 4);

        DomainValidationException exception = assertThrows(
                DomainValidationException.class,
                () -> validator.validate("a", annotation, "text")
        );

        assertEquals(ValidationExceptionCode.INVALID_CODE_POINT_LENGTH, exception.getCode());
    }
}