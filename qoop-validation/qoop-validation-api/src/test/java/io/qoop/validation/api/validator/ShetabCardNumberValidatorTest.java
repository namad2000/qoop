package io.qoop.validation.api.validator;

import io.qoop.fault.handler.api.exception.DomainValidationException;
import io.qoop.validation.api.ShetabCardNumber;
import io.qoop.validation.api.exception.ValidationExceptionCode;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.lang.annotation.Annotation;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for {@link ShetabCardNumberValidator}.
 *
 * @author Davood Akbari - 1404
 * daak1365@gmail.com
 * daak1365@yahoo.com
 * 09125188694
 */
class ShetabCardNumberValidatorTest {

    private ShetabCardNumberValidator validator;

    @BeforeEach
    void setUp() {
        validator = new ShetabCardNumberValidator();
    }

    private ShetabCardNumber createShetabCardNumber() {
        return new ShetabCardNumber() {
            @Override
            public Class<? extends Annotation> annotationType() {
                return ShetabCardNumber.class;
            }
        };
    }

    @Test
    @DisplayName("Should pass when value is null or empty")
    void validate_NullOrEmptyValue_Success() {
        ShetabCardNumber annotation = createShetabCardNumber();
        assertDoesNotThrow(() -> validator.validate(null, annotation, "cardNumber"));
        assertDoesNotThrow(() -> validator.validate("", annotation, "cardNumber"));
        assertDoesNotThrow(() -> validator.validate("   ", annotation, "cardNumber"));
    }

    @ParameterizedTest
    @ValueSource(strings = {
            "6037997599999993",        // Sum = 110 (Valid Bank Melli)
            "6219861012345673",        // Sum = 60  (Valid Saman Bank)
            "6037-9975-9999-9993",     // Valid card with dashes
            "6037 9975 9999 9993"      // Valid card with spaces
    })
    @DisplayName("Should pass when Shetab card number is valid")
    void validate_ValidCardNumber_Success(String input) {
        ShetabCardNumber annotation = createShetabCardNumber();
        assertDoesNotThrow(() -> validator.validate(input, annotation, "cardNumber"));
    }

    @ParameterizedTest
    @ValueSource(strings = {
            "6037997599999998",          // Invalid checksum digit
            "1234567890123456",          // Invalid card
            "60379975999999",            // 14 digits (less than 16)
            "invalid-card-num"           // Non-numeric characters
    })
    @DisplayName("Should throw exception when Shetab card number is invalid")
    void validate_InvalidCardNumber_ThrowsDomainValidationException(String input) {
        ShetabCardNumber annotation = createShetabCardNumber();

        DomainValidationException exception = assertThrows(
                DomainValidationException.class,
                () -> validator.validate(input, annotation, "cardNumber")
        );

        assertEquals(ValidationExceptionCode.INVALID_SHETAB_CARD_NUMBER, exception.getCode());
    }
}