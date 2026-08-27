package io.qoop.validation.api.validator;

import io.qoop.fault.handler.api.exception.DomainValidationException;
import io.qoop.validation.api.NotNull;
import io.qoop.validation.api.exception.ValidationExceptionCode;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.lang.annotation.Annotation;

import static org.junit.jupiter.api.Assertions.*;

class NotNullValidatorTest {

    private NotNullValidator validator;

    @BeforeEach
    void setUp() {
        validator = new NotNullValidator();
    }

    private NotNull createNotNull() {
        return new NotNull() {
            @Override
            public Class<? extends Annotation> annotationType() {
                return NotNull.class;
            }
        };
    }

    @Test
    @DisplayName("Should pass when value is non-null")
    void validate_NonNullValue_Success() {
        NotNull annotation = createNotNull();

        assertDoesNotThrow(() -> validator.validate("test", annotation, "fieldName"));
        assertDoesNotThrow(() -> validator.validate(123, annotation, "fieldName"));
        assertDoesNotThrow(() -> validator.validate("", annotation, "fieldName"));
        assertDoesNotThrow(() -> validator.validate(new Object(), annotation, "fieldName"));
    }

    @Test
    @DisplayName("Should throw exception when value is null")
    void validate_NullValue_ThrowsDomainValidationException() {
        NotNull annotation = createNotNull();

        DomainValidationException exception = assertThrows(
                DomainValidationException.class,
                () -> validator.validate(null, annotation, "fieldName")
        );

        assertEquals(ValidationExceptionCode.NOT_NULL, exception.getCode());
    }
}