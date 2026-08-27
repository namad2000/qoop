package io.qoop.validation.api.validator;

import io.qoop.fault.handler.api.exception.DomainValidationException;
import io.qoop.validation.api.Future;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZonedDateTime;
import java.util.Calendar;
import java.util.Date;

import static io.qoop.validation.api.exception.ValidationExceptionCode.MUST_BE_IN_FUTURE;

/**
 * @author Davood Akbari - 1404
 * daak1365@gmail.com
 * daak1365@yahoo.com
 * 09125188694
 */
public class FutureValidator implements AnnotationValidator<Object, Future> {

    @Override
    public void validate(Object value, Future annotation, String paramName) {
        if (value == null) {
            return;
        }

        if (!isFuture(value)) {
            throw DomainValidationException.withParams(MUST_BE_IN_FUTURE, paramName, paramName);
        }
    }

    private boolean isFuture(Object value) {
        if (value instanceof Date) {
            return ((Date) value).after(new Date());
        }
        if (value instanceof Calendar) {
            return ((Calendar) value).after(Calendar.getInstance());
        }
        if (value instanceof LocalDate) {
            return ((LocalDate) value).isAfter(LocalDate.now());
        }
        if (value instanceof LocalDateTime) {
            return ((LocalDateTime) value).isAfter(LocalDateTime.now());
        }
        if (value instanceof ZonedDateTime) {
            return ((ZonedDateTime) value).isAfter(ZonedDateTime.now());
        }
        if (value instanceof OffsetDateTime) {
            return ((OffsetDateTime) value).isAfter(OffsetDateTime.now());
        }
        if (value instanceof Instant) {
            return ((Instant) value).isAfter(Instant.now());
        }
        return false;
    }
}