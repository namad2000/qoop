package io.qoop.validation.api.validator;

import io.qoop.fault.handler.api.exception.DomainValidationException;
import io.qoop.validation.api.PastOrPresent;
import io.qoop.validation.api.exception.ValidationExceptionCode;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.lang.annotation.Annotation;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Date;

import static org.junit.jupiter.api.Assertions.*;

class PastOrPresentValidatorTest {

    private PastOrPresentValidator validator;

    @BeforeEach
    void setUp() {
        validator = new PastOrPresentValidator();
    }

    private PastOrPresent createPastOrPresent() {
        return new PastOrPresent() {
            @Override
            public Class<? extends Annotation> annotationType() {
                return PastOrPresent.class;
            }
        };
    }

    @Test
    @DisplayName("Should pass when value is null")
    void validate_NullValue_Success() {
        PastOrPresent annotation = createPastOrPresent();
        assertDoesNotThrow(() -> validator.validate(null, annotation, "birthDate"));
    }

    @Test
    @DisplayName("Should pass when value is in the past or present")
    void validate_PastOrPresentDate_Success() {
        PastOrPresent annotation = createPastOrPresent();

        assertDoesNotThrow(() -> validator.validate(LocalDate.now().minusDays(1), annotation, "birthDate"));
        assertDoesNotThrow(() -> validator.validate(LocalDate.now(), annotation, "birthDate"));
        assertDoesNotThrow(() -> validator.validate(new Date(System.currentTimeMillis() - 10000), annotation, "birthDate"));
        assertDoesNotThrow(() -> validator.validate(LocalDateTime.now(), annotation, "birthDate"));
    }

    @Test
    @DisplayName("Should throw exception when value is in the future")
    void validate_FutureDate_ThrowsDomainValidationException() {
        PastOrPresent annotation = createPastOrPresent();

        DomainValidationException exception = assertThrows(
                DomainValidationException.class,
                () -> validator.validate(LocalDate.now().plusDays(1), annotation, "birthDate")
        );

        assertEquals(ValidationExceptionCode.MUST_BE_IN_PAST_OR_PRESENT, exception.getCode());
    }
}