package io.qoop.validation.api.validator;

import io.qoop.fault.handler.api.exception.DomainValidationException;
import io.qoop.validation.api.Range;
import io.qoop.validation.api.exception.ValidationExceptionCode;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.lang.annotation.Annotation;
import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

class RangeValidatorTest {

    private RangeValidator validator;

    @BeforeEach
    void setUp() {
        validator = new RangeValidator();
    }

    private Range createRange(long min, long max) {
        return new Range() {
            @Override
            public Class<? extends Annotation> annotationType() {
                return Range.class;
            }

            @Override
            public long min() {
                return min;
            }

            @Override
            public long max() {
                return max;
            }
        };
    }

    @Test
    @DisplayName("Should pass when value is null")
    void validate_NullValue_Success() {
        Range annotation = createRange(10, 100);
        assertDoesNotThrow(() -> validator.validate(null, annotation, "age"));
    }

    @Test
    @DisplayName("Should pass when value is within numeric range")
    void validate_ValidRange_Success() {
        Range annotation = createRange(10, 100);

        assertDoesNotThrow(() -> validator.validate(10, annotation, "age"));
        assertDoesNotThrow(() -> validator.validate(50L, annotation, "age"));
        assertDoesNotThrow(() -> validator.validate(100, annotation, "age"));
        assertDoesNotThrow(() -> validator.validate("75", annotation, "age"));
        assertDoesNotThrow(() -> validator.validate(new BigDecimal("99.9"), annotation, "age"));
    }

    @Test
    @DisplayName("Should throw exception when value is out of range")
    void validate_InvalidRange_ThrowsDomainValidationException() {
        Range annotation = createRange(10, 100);

        DomainValidationException exMin = assertThrows(
                DomainValidationException.class,
                () -> validator.validate(9, annotation, "age")
        );
        assertEquals(ValidationExceptionCode.INVALID_RANGE, exMin.getCode());

        DomainValidationException exMax = assertThrows(
                DomainValidationException.class,
                () -> validator.validate(101, annotation, "age")
        );
        assertEquals(ValidationExceptionCode.INVALID_RANGE, exMax.getCode());
    }
}