package io.qoop.validation.api.validator;

import io.qoop.fault.handler.api.exception.DomainValidationException;
import io.qoop.validation.api.ForeignCode;
import io.qoop.validation.api.exception.ValidationExceptionCode;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.lang.annotation.Annotation;

import static org.junit.jupiter.api.Assertions.*;

class ForeignCodeValidatorTest {

    private ForeignCodeValidator validator;

    @BeforeEach
    void setUp() {
        validator = new ForeignCodeValidator();
    }

    private ForeignCode createForeignCode() {
        return new ForeignCode() {
            @Override
            public Class<? extends Annotation> annotationType() {
                return ForeignCode.class;
            }
        };
    }

    @Test
    @DisplayName("Should pass when value is null or empty")
    void validate_NullOrEmptyValue_Success() {
        ForeignCode annotation = createForeignCode();
        assertDoesNotThrow(() -> validator.validate(null, annotation, "foreignCode"));
        assertDoesNotThrow(() -> validator.validate("", annotation, "foreignCode"));
    }

    @ParameterizedTest
    @ValueSource(strings = {
            "9781234567897", // sum = 243 -> rem = 3 -> checkDigit = 10 - 3 = 7
            "1000000000007", // sum = 3   -> rem = 3 -> checkDigit = 10 - 3 = 7
            "2000000000004", // sum = 6   -> rem = 6 -> checkDigit = 10 - 6 = 4
            "0000000000000"  // sum = 0   -> rem = 0 -> checkDigit = 10 - 0 = 0
    })
    @DisplayName("Should pass when foreign code is valid")
    void validate_ValidForeignCode_Success(String input) {
        ForeignCode annotation = createForeignCode();
        assertDoesNotThrow(() -> validator.validate(input, annotation, "foreignCode"));
    }

    @ParameterizedTest
    @ValueSource(strings = {
            "9781234567890", // Invalid check digit (expects 7)
            "1000000000003", // Invalid check digit (expects 7)
            "1234567890123", // Invalid check digit
            "123456"         // Invalid length
    })
    @DisplayName("Should throw exception when foreign code is invalid")
    void validate_InvalidForeignCode_ThrowsDomainValidationException(String input) {
        ForeignCode annotation = createForeignCode();

        DomainValidationException exception = assertThrows(
                DomainValidationException.class,
                () -> validator.validate(input, annotation, "foreignCode")
        );

        assertEquals(ValidationExceptionCode.INVALID_FOREIGN_CODE, exception.getCode());
    }
}