package io.qoop.validation.api.validator;

import io.qoop.fault.handler.api.exception.DomainValidationException;
import io.qoop.validation.api.NotBlank;
import io.qoop.validation.api.exception.ValidationExceptionCode;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;

import java.lang.annotation.Annotation;

import static org.junit.jupiter.api.Assertions.*;

class NotBlankValidatorTest {

    private NotBlankValidator validator;

    @BeforeEach
    void setUp() {
        validator = new NotBlankValidator();
    }

    private NotBlank createNotBlank() {
        return new NotBlank() {
            @Override
            public Class<? extends Annotation> annotationType() {
                return NotBlank.class;
            }
        };
    }

    @Test
    @DisplayName("Should pass when value contains non-whitespace characters")
    void validate_ValidString_Success() {
        NotBlank annotation = createNotBlank();

        assertDoesNotThrow(() -> validator.validate("validString", annotation, "username"));
        assertDoesNotThrow(() -> validator.validate("  valid  ", annotation, "username"));
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {" ", "   ", "\t", "\n", "\r\n"})
    @DisplayName("Should throw exception when value is null, empty, or whitespace")
    void validate_BlankOrNullValue_ThrowsDomainValidationException(String input) {
        NotBlank annotation = createNotBlank();

        DomainValidationException exception = assertThrows(
                DomainValidationException.class,
                () -> validator.validate(input, annotation, "username")
        );

        assertEquals(ValidationExceptionCode.NOT_BLANK, exception.getCode());
    }
}