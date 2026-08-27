package io.qoop.validation.api.validator;

import io.qoop.fault.handler.api.exception.DomainValidationException;
import io.qoop.validation.api.AssertTrue;
import io.qoop.validation.api.exception.ValidationExceptionCode;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.lang.annotation.Annotation;

import static org.junit.jupiter.api.Assertions.*;

class AssertTrueValidatorTest {

    private AssertTrueValidator validator;

    @BeforeEach
    void setUp() {
        validator = new AssertTrueValidator();
    }

    private AssertTrue createAssertTrue() {
        return new AssertTrue() {
            @Override
            public Class<? extends Annotation> annotationType() {
                return AssertTrue.class;
            }
        };
    }

    @Test
    @DisplayName("Should pass when value is null or Boolean.TRUE")
    void validate_NullOrTrueValue_Success() {
        AssertTrue annotation = createAssertTrue();
        assertDoesNotThrow(() -> validator.validate(null, annotation, "acceptedTerms"));
        assertDoesNotThrow(() -> validator.validate(Boolean.TRUE, annotation, "acceptedTerms"));
    }

    @Test
    @DisplayName("Should throw exception when value is Boolean.FALSE")
    void validate_FalseValue_ThrowsDomainValidationException() {
        AssertTrue annotation = createAssertTrue();

        DomainValidationException exception = assertThrows(
                DomainValidationException.class,
                () -> validator.validate(Boolean.FALSE, annotation, "acceptedTerms")
        );

        assertEquals(ValidationExceptionCode.MUST_BE_TRUE, exception.getCode());
    }
}