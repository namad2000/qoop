package io.qoop.validation.api.validator;

import io.qoop.fault.handler.api.exception.DomainValidationException;
import io.qoop.validation.api.FutureOrPresent;
import io.qoop.validation.api.exception.ValidationExceptionCode;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.lang.annotation.Annotation;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Date;

import static org.junit.jupiter.api.Assertions.*;

class FutureOrPresentValidatorTest {

    private FutureOrPresentValidator validator;

    @BeforeEach
    void setUp() {
        validator = new FutureOrPresentValidator();
    }

    private FutureOrPresent createFutureOrPresent() {
        return new FutureOrPresent() {
            @Override
            public Class<? extends Annotation> annotationType() {
                return FutureOrPresent.class;
            }
        };
    }

    @Test
    @DisplayName("Should pass when value is null")
    void validate_NullValue_Success() {
        FutureOrPresent annotation = createFutureOrPresent();
        assertDoesNotThrow(() -> validator.validate(null, annotation, "expirationDate"));
    }

    @Test
    @DisplayName("Should pass when value is in the future or present")
    void validate_FutureOrPresentDate_Success() {
        FutureOrPresent annotation = createFutureOrPresent();

        assertDoesNotThrow(() -> validator.validate(LocalDate.now().plusDays(1), annotation, "expirationDate"));
        assertDoesNotThrow(() -> validator.validate(LocalDate.now(), annotation, "expirationDate"));
        assertDoesNotThrow(() -> validator.validate(new Date(System.currentTimeMillis() + 10000), annotation, "expirationDate"));
        assertDoesNotThrow(() -> validator.validate(LocalDateTime.now().plusHours(1), annotation, "expirationDate"));
    }

    @Test
    @DisplayName("Should throw exception when value is in the past")
    void validate_PastDate_ThrowsDomainValidationException() {
        FutureOrPresent annotation = createFutureOrPresent();

        DomainValidationException exception = assertThrows(
                DomainValidationException.class,
                () -> validator.validate(LocalDate.now().minusDays(1), annotation, "expirationDate")
        );

        assertEquals(ValidationExceptionCode.MUST_BE_IN_FUTURE_OR_PRESENT, exception.getCode());
    }
}