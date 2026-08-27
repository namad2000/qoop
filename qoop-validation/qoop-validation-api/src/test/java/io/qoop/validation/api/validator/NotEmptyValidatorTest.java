package io.qoop.validation.api.validator;

import io.qoop.fault.handler.api.exception.DomainValidationException;
import io.qoop.validation.api.NotEmpty;
import io.qoop.validation.api.exception.ValidationExceptionCode;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.lang.annotation.Annotation;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class NotEmptyValidatorTest {

    private NotEmptyValidator validator;

    @BeforeEach
    void setUp() {
        validator = new NotEmptyValidator();
    }

    private NotEmpty createNotEmpty() {
        return new NotEmpty() {
            @Override
            public Class<? extends Annotation> annotationType() {
                return NotEmpty.class;
            }
        };
    }

    @Test
    @DisplayName("Should pass when String, Collection, Map, or Array is not empty")
    void validate_NonEmptyValues_Success() {
        NotEmpty annotation = createNotEmpty();

        assertDoesNotThrow(() -> validator.validate("text", annotation, "items"));
        assertDoesNotThrow(() -> validator.validate(List.of("item"), annotation, "items"));
        assertDoesNotThrow(() -> validator.validate(Map.of("key", "value"), annotation, "items"));
        assertDoesNotThrow(() -> validator.validate(new int[]{1, 2, 3}, annotation, "items"));
    }

    @Test
    @DisplayName("Should throw exception when value is null")
    void validate_NullValue_ThrowsDomainValidationException() {
        NotEmpty annotation = createNotEmpty();

        DomainValidationException exception = assertThrows(
                DomainValidationException.class,
                () -> validator.validate(null, annotation, "items")
        );

        assertEquals(ValidationExceptionCode.NOT_EMPTY, exception.getCode());
    }

    @Test
    @DisplayName("Should throw exception when String is empty")
    void validate_EmptyString_ThrowsDomainValidationException() {
        NotEmpty annotation = createNotEmpty();

        DomainValidationException exception = assertThrows(
                DomainValidationException.class,
                () -> validator.validate("", annotation, "items")
        );

        assertEquals(ValidationExceptionCode.NOT_EMPTY, exception.getCode());
    }

    @Test
    @DisplayName("Should throw exception when Collection is empty")
    void validate_EmptyCollection_ThrowsDomainValidationException() {
        NotEmpty annotation = createNotEmpty();

        DomainValidationException exception = assertThrows(
                DomainValidationException.class,
                () -> validator.validate(Collections.emptyList(), annotation, "items")
        );

        assertEquals(ValidationExceptionCode.NOT_EMPTY, exception.getCode());
    }

    @Test
    @DisplayName("Should throw exception when Map is empty")
    void validate_EmptyMap_ThrowsDomainValidationException() {
        NotEmpty annotation = createNotEmpty();

        DomainValidationException exception = assertThrows(
                DomainValidationException.class,
                () -> validator.validate(Collections.emptyMap(), annotation, "items")
        );

        assertEquals(ValidationExceptionCode.NOT_EMPTY, exception.getCode());
    }

    @Test
    @DisplayName("Should throw exception when Array is empty")
    void validate_EmptyArray_ThrowsDomainValidationException() {
        NotEmpty annotation = createNotEmpty();

        DomainValidationException exception = assertThrows(
                DomainValidationException.class,
                () -> validator.validate(new String[0], annotation, "items")
        );

        assertEquals(ValidationExceptionCode.NOT_EMPTY, exception.getCode());
    }
}