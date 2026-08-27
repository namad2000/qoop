package io.qoop.validation.api.validator;

import io.qoop.fault.handler.api.exception.DomainValidationException;
import io.qoop.validation.api.LegalNationalId;
import io.qoop.validation.api.exception.ValidationExceptionCode;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.lang.annotation.Annotation;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for {@link LegalNationalIdValidator}.
 *
 * @author Davood Akbari - 1404
 * daak1365@gmail.com
 * daak1365@yahoo.com
 * 09125188694
 */
class LegalNationalIdValidatorTest {

    private LegalNationalIdValidator validator;

    @BeforeEach
    void setUp() {
        validator = new LegalNationalIdValidator();
    }

    private LegalNationalId createLegalNationalId() {
        return new LegalNationalId() {
            @Override
            public Class<? extends Annotation> annotationType() {
                return LegalNationalId.class;
            }
        };
    }

    @Test
    @DisplayName("Should pass when value is null or empty")
    void validate_NullOrEmptyValue_Success() {
        LegalNationalId annotation = createLegalNationalId();
        assertDoesNotThrow(() -> validator.validate(null, annotation, "legalNationalId"));
        assertDoesNotThrow(() -> validator.validate("", annotation, "legalNationalId"));
        assertDoesNotThrow(() -> validator.validate("   ", annotation, "legalNationalId"));
    }

    @ParameterizedTest
    @ValueSource(strings = {
            "10100100002"          // Mathematically verified valid legal ID
    })
    @DisplayName("Should pass when Legal National ID is valid")
    void validate_ValidLegalNationalId_Success(String input) {
        LegalNationalId annotation = createLegalNationalId();
        assertDoesNotThrow(() -> validator.validate(input, annotation, "legalNationalId"));
    }

    @ParameterizedTest
    @ValueSource(strings = {
            "10100000009",          // Invalid check digit
            "12345678901",          // Invalid structure
            "1010000000",           // Too short
            "101000000089",         // Too long
            "1010000000A"           // Non-numeric
    })
    @DisplayName("Should throw exception when Legal National ID is invalid")
    void validate_InvalidLegalNationalId_ThrowsDomainValidationException(String input) {
        LegalNationalId annotation = createLegalNationalId();

        DomainValidationException exception = assertThrows(
                DomainValidationException.class,
                () -> validator.validate(input, annotation, "legalNationalId")
        );

        assertEquals(ValidationExceptionCode.INVALID_LEGAL_NATIONAL_ID, exception.getCode());
    }
}