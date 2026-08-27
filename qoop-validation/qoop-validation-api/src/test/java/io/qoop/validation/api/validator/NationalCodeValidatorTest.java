package io.qoop.validation.api.validator;

import io.qoop.fault.handler.api.exception.DomainValidationException;
import io.qoop.validation.api.NationalCode;
import io.qoop.validation.api.exception.ValidationExceptionCode;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.lang.annotation.Annotation;

import static org.junit.jupiter.api.Assertions.*;

class NationalCodeValidatorTest {

    private NationalCodeValidator validator;

    @BeforeEach
    void setUp() {
        validator = new NationalCodeValidator();
    }

    private NationalCode createNationalCode() {
        return new NationalCode() {
            @Override
            public Class<? extends Annotation> annotationType() {
                return NationalCode.class;
            }
        };
    }

    @Test
    @DisplayName("Should pass when value is null")
    void validate_NullValue_Success() {
        NationalCode annotation = createNationalCode();

        assertDoesNotThrow(() -> validator.validate(null, annotation, "nationalCode"));
    }

    @Test
    @DisplayName("Should pass when value is empty")
    void validate_EmptyValue_Success() {
        NationalCode annotation = createNationalCode();

        assertDoesNotThrow(() -> validator.validate("", annotation, "nationalCode"));
    }

    @ParameterizedTest
    @ValueSource(strings = {
            "0010000003"
    })
    @DisplayName("Should pass when national code is valid")
    void validate_ValidNationalCode_Success(String input) {
        NationalCode annotation = createNationalCode();

        assertDoesNotThrow(() ->
                validator.validate(input, annotation, "nationalCode"));
    }

    @ParameterizedTest
    @ValueSource(strings = {
            "0010000000",
            "0010000001",
            "0010000002",
            "0010000004",
            "0010000005",
            "0080000012",
            "0060000017",
            "0012345672",
            "1111111111",
            "2222222222",
            "3333333333",
            "4444444444",
            "5555555555",
            "6666666666",
            "7777777777",
            "8888888888",
            "9999999999",
            "1234567890",
            "123456789",
            "12345678901",
            "12345",
            "000000000",
            "00000000000",
            "abc1234567",
            "abcdefghij",
            "123456789a",
            "abcdefgh12",
            "12-34567890"
    })
    @DisplayName("Should throw exception when national code is invalid")
    void validate_InvalidNationalCode_ThrowsException(String input) {
        NationalCode annotation = createNationalCode();

        DomainValidationException exception =
                assertThrows(DomainValidationException.class,
                        () -> validator.validate(
                                input, annotation, "nationalCode"));

        assertEquals(ValidationExceptionCode.INVALID_NATIONAL_CODE, exception.getCode());
    }
}