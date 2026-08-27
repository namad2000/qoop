package io.qoop.validation.api.validator;

import io.qoop.fault.handler.api.exception.DomainValidationException;
import io.qoop.validation.api.exception.ValidationExceptionCode;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.lang.annotation.Annotation;

import static org.junit.jupiter.api.Assertions.*;

class IranianMobileNumberValidatorTest {

    private IranianMobileNumberValidator validator;

    @BeforeEach
    void setUp() {
        validator = new IranianMobileNumberValidator();
    }

    private IranianMobileNumber createIranianMobileNumber() {
        return new IranianMobileNumber() {
            @Override
            public Class<? extends Annotation> annotationType() {
                return IranianMobileNumber.class;
            }
        };
    }

    @Test
    @DisplayName("Should pass when value is null or empty")
    void validate_NullOrEmptyValue_Success() {
        IranianMobileNumber annotation = createIranianMobileNumber();
        assertDoesNotThrow(() -> validator.validate(null, annotation, "mobileNumber"));
        assertDoesNotThrow(() -> validator.validate("", annotation, "mobileNumber"));
    }

    @ParameterizedTest
    @ValueSource(strings = {"09125188694", "+989125188694", "00989125188694", "9125188694"})
    @DisplayName("Should pass when Iranian mobile number is valid")
    void validate_ValidMobileNumber_Success(String input) {
        IranianMobileNumber annotation = createIranianMobileNumber();
        assertDoesNotThrow(() -> validator.validate(input, annotation, "mobileNumber"));
    }

    @ParameterizedTest
    @ValueSource(strings = {"08125188694", "0912518869", "091251886944", "invalid-mobile"})
    @DisplayName("Should throw exception when Iranian mobile number is invalid")
    void validate_InvalidMobileNumber_ThrowsDomainValidationException(String input) {
        IranianMobileNumber annotation = createIranianMobileNumber();

        DomainValidationException exception = assertThrows(
                DomainValidationException.class,
                () -> validator.validate(input, annotation, "mobileNumber")
        );

        assertEquals(ValidationExceptionCode.INVALID_IRANIAN_MOBILE_NUMBER, exception.getCode());
    }
}