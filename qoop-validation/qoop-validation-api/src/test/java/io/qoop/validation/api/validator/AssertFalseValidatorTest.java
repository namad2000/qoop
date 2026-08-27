package io.qoop.validation.api.validator;

import io.qoop.fault.handler.api.exception.DomainValidationException;
import io.qoop.validation.api.AssertFalse;
import io.qoop.validation.api.exception.ValidationExceptionCode;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.lang.annotation.Annotation;

import static org.junit.jupiter.api.Assertions.*;

class AssertFalseValidatorTest {

    private AssertFalseValidator validator;

    @BeforeEach
    void setUp() {
        validator = new AssertFalseValidator();
    }

    private AssertFalse createAssertFalse() {
        return new AssertFalse() {
            @Override
            public Class<? extends Annotation> annotationType() {
                return AssertFalse.class;
            }
        };
    }

    @Test
    @DisplayName("Should pass when value is null or Boolean.FALSE")
    void validate_NullOrFalseValue_Success() {
        AssertFalse annotation = createAssertFalse();
        assertDoesNotThrow(() -> validator.validate(null, annotation, "isLocked"));
        assertDoesNotThrow(() -> validator.validate(Boolean.FALSE, annotation, "isLocked"));
    }

    @Test
    @DisplayName("Should throw exception when value is Boolean.TRUE")
    void validate_TrueValue_ThrowsDomainValidationException() {
        AssertFalse annotation = createAssertFalse();

        DomainValidationException exception = assertThrows(
                DomainValidationException.class,
                () -> validator.validate(Boolean.TRUE, annotation, "isLocked")
        );

        assertEquals(ValidationExceptionCode.MUST_BE_FALSE, exception.getCode());
    }
}