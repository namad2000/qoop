package io.qoop.validation.api.validator;

import io.qoop.fault.handler.api.exception.DomainValidationException;
import io.qoop.validation.api.Past;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZonedDateTime;
import java.util.Calendar;
import java.util.Date;

import static io.qoop.validation.api.exception.ValidationExceptionCode.MUST_BE_IN_PAST;

/**
 * @author Davood Akbari - 1404
 * daak1365@gmail.com
 * daak1365@yahoo.com
 * 09125188694
 */
public class PastValidator implements AnnotationValidator<Object, Past> {

    @Override
    public void validate(Object value, Past annotation, String paramName) {
        if (value == null) {
            return;
        }

        if (!isPast(value)) {
            throw DomainValidationException.withParams(MUST_BE_IN_PAST, paramName, paramName);
        }
    }

    private boolean isPast(Object value) {
        if (value instanceof Date) {
            return ((Date) value).before(new Date());
        }
        if (value instanceof Calendar) {
            return ((Calendar) value).before(Calendar.getInstance());
        }
        if (value instanceof LocalDate) {
            return ((LocalDate) value).isBefore(LocalDate.now());
        }
        if (value instanceof LocalDateTime) {
            return ((LocalDateTime) value).isBefore(LocalDateTime.now());
        }
        if (value instanceof ZonedDateTime) {
            return ((ZonedDateTime) value).isBefore(ZonedDateTime.now());
        }
        if (value instanceof OffsetDateTime) {
            return ((OffsetDateTime) value).isBefore(OffsetDateTime.now());
        }
        if (value instanceof Instant) {
            return ((Instant) value).isBefore(Instant.now());
        }
        return false;
    }
}