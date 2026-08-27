package io.qoop.validation.api.validator;

import io.qoop.fault.handler.api.exception.DomainValidationException;
import io.qoop.validation.api.IranianPostalCode;
import io.qoop.validation.api.exception.ValidationExceptionCode;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.lang.annotation.Annotation;

import static org.junit.jupiter.api.Assertions.*;

class IranianPostalCodeValidatorTest {

    private IranianPostalCodeValidator validator;

    @BeforeEach
    void setUp() {
        validator = new IranianPostalCodeValidator();
    }

    private IranianPostalCode createIranianPostalCode() {
        return new IranianPostalCode() {
            @Override
            public Class<? extends Annotation> annotationType() {
                return IranianPostalCode.class;
            }
        };
    }

    @Test
    @DisplayName("Should pass when value is null or empty")
    void validate_NullOrEmptyValue_Success() {
        IranianPostalCode annotation = createIranianPostalCode();
        assertDoesNotThrow(() -> validator.validate(null, annotation, "postalCode"));
        assertDoesNotThrow(() -> validator.validate("", annotation, "postalCode"));
    }

    @ParameterizedTest
    @ValueSource(strings = {"1193653471", "1983963113", "1543813111"})
    @DisplayName("Should pass when Iranian postal code is valid")
    void validate_ValidPostalCode_Success(String input) {
        IranianPostalCode annotation = createIranianPostalCode();
        assertDoesNotThrow(() -> validator.validate(input, annotation, "postalCode"));
    }

    @ParameterizedTest
    @ValueSource(strings = {"0123456789", "2123456789", "12345", "11936534710", "invalid-code"})
    @DisplayName("Should throw exception when Iranian postal code is invalid")
    void validate_InvalidPostalCode_ThrowsDomainValidationException(String input) {
        IranianPostalCode annotation = createIranianPostalCode();

        DomainValidationException exception = assertThrows(
                DomainValidationException.class,
                () -> validator.validate(input, annotation, "postalCode")
        );

        assertEquals(ValidationExceptionCode.INVALID_IRANIAN_POSTAL_CODE, exception.getCode());
    }
}