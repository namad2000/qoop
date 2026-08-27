package io.qoop.validation.api.validator;

import io.qoop.fault.handler.api.exception.DomainValidationException;
import io.qoop.validation.api.Past;
import io.qoop.validation.api.exception.ValidationExceptionCode;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.lang.annotation.Annotation;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Date;

import static org.junit.jupiter.api.Assertions.*;

class PastValidatorTest {

    private PastValidator validator;

    @BeforeEach
    void setUp() {
        validator = new PastValidator();
    }

    private Past createPast() {
        return new Past() {
            @Override
            public Class<? extends Annotation> annotationType() {
                return Past.class;
            }
        };
    }

    @Test
    @DisplayName("Should pass when value is null")
    void validate_NullValue_Success() {
        Past annotation = createPast();
        assertDoesNotThrow(() -> validator.validate(null, annotation, "birthDate"));
    }

    @Test
    @DisplayName("Should pass when date is in the past")
    void validate_PastDate_Success() {
        Past annotation = createPast();
        assertDoesNotThrow(() -> validator.validate(LocalDate.now().minusDays(1), annotation, "birthDate"));
        assertDoesNotThrow(() -> validator.validate(LocalDateTime.now().minusHours(1), annotation, "birthDate"));
        assertDoesNotThrow(() -> validator.validate(new Date(System.currentTimeMillis() - 100000), annotation, "birthDate"));
    }

    @Test
    @DisplayName("Should throw exception when date is in the future")
    void validate_FutureDate_ThrowsDomainValidationException() {
        Past annotation = createPast();

        DomainValidationException exception = assertThrows(
                DomainValidationException.class,
                () -> validator.validate(LocalDate.now().plusDays(1), annotation, "birthDate")
        );

        assertEquals(ValidationExceptionCode.MUST_BE_IN_PAST, exception.getCode());
    }
}