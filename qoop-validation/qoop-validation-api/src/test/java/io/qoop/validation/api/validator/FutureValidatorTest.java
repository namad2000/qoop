package io.qoop.validation.api.validator;

import io.qoop.fault.handler.api.exception.DomainValidationException;
import io.qoop.validation.api.Future;
import io.qoop.validation.api.exception.ValidationExceptionCode;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.lang.annotation.Annotation;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Date;

import static org.junit.jupiter.api.Assertions.*;

class FutureValidatorTest {

    private FutureValidator validator;

    @BeforeEach
    void setUp() {
        validator = new FutureValidator();
    }

    private Future createFuture() {
        return new Future() {
            @Override
            public Class<? extends Annotation> annotationType() {
                return Future.class;
            }
        };
    }

    @Test
    @DisplayName("Should pass when value is null")
    void validate_NullValue_Success() {
        Future annotation = createFuture();
        assertDoesNotThrow(() -> validator.validate(null, annotation, "expireDate"));
    }

    @Test
    @DisplayName("Should pass when date is in the future")
    void validate_FutureDate_Success() {
        Future annotation = createFuture();
        assertDoesNotThrow(() -> validator.validate(LocalDate.now().plusDays(1), annotation, "expireDate"));
        assertDoesNotThrow(() -> validator.validate(LocalDateTime.now().plusHours(1), annotation, "expireDate"));
        assertDoesNotThrow(() -> validator.validate(new Date(System.currentTimeMillis() + 100000), annotation, "expireDate"));
    }

    @Test
    @DisplayName("Should throw exception when date is in the past")
    void validate_PastDate_ThrowsDomainValidationException() {
        Future annotation = createFuture();

        DomainValidationException exception = assertThrows(
                DomainValidationException.class,
                () -> validator.validate(LocalDate.now().minusDays(1), annotation, "expireDate")
        );

        assertEquals(ValidationExceptionCode.MUST_BE_IN_FUTURE, exception.getCode());
    }
}