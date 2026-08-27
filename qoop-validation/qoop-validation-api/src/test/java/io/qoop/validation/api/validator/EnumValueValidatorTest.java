package io.qoop.validation.api.validator;

import io.qoop.fault.handler.api.exception.DomainValidationException;
import io.qoop.validation.api.EnumValue;
import io.qoop.validation.api.exception.ValidationExceptionCode;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.lang.annotation.Annotation;

import static org.junit.jupiter.api.Assertions.*;

class EnumValueValidatorTest {

    private EnumValueValidator validator;

    enum TestStatus {
        ACTIVE,
        INACTIVE,
        PENDING
    }

    @BeforeEach
    void setUp() {
        validator = new EnumValueValidator();
    }

    private EnumValue createEnumValue(Class<? extends Enum<?>> enumClass, boolean ignoreCase) {
        return new EnumValue() {
            @Override
            public Class<? extends Annotation> annotationType() {
                return EnumValue.class;
            }

            @Override
            public Class<? extends Enum<?>> enumClass() {
                return enumClass;
            }

            @Override
            public boolean ignoreCase() {
                return ignoreCase;
            }
        };
    }

    @Test
    @DisplayName("Should pass when value is null")
    void validate_NullValue_Success() {
        EnumValue annotation = createEnumValue(TestStatus.class, false);
        assertDoesNotThrow(() -> validator.validate(null, annotation, "status"));
    }

    @Test
    @DisplayName("Should pass when value matches enum constant name exactly")
    void validate_ValidEnumValue_Success() {
        EnumValue annotation = createEnumValue(TestStatus.class, false);

        assertDoesNotThrow(() -> validator.validate("ACTIVE", annotation, "status"));
        assertDoesNotThrow(() -> validator.validate(TestStatus.PENDING, annotation, "status"));
    }

    @Test
    @DisplayName("Should pass when ignoreCase is true and casing differs")
    void validate_IgnoreCaseEnumValue_Success() {
        EnumValue annotation = createEnumValue(TestStatus.class, true);

        assertDoesNotThrow(() -> validator.validate("active", annotation, "status"));
        assertDoesNotThrow(() -> validator.validate("InAcTiVe", annotation, "status"));
    }

    @ParameterizedTest
    @ValueSource(strings = {"INVALID", "active", "DELETED"})
    @DisplayName("Should throw exception when value is not in enum (strict casing)")
    void validate_InvalidEnumValue_ThrowsDomainValidationException(String input) {
        EnumValue annotation = createEnumValue(TestStatus.class, false);

        DomainValidationException exception = assertThrows(
                DomainValidationException.class,
                () -> validator.validate(input, annotation, "status")
        );

        assertEquals(ValidationExceptionCode.INVALID_ENUM_VALUE, exception.getCode());
    }
}