package io.qoop.validation.api.validator;

import io.qoop.fault.handler.api.exception.DomainValidationException;
import io.qoop.validation.api.PastOrPresent;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.chrono.ChronoLocalDate;
import java.time.chrono.ChronoLocalDateTime;
import java.time.chrono.ChronoZonedDateTime;
import java.util.Date;

import static io.qoop.validation.api.exception.ValidationExceptionCode.MUST_BE_IN_PAST_OR_PRESENT;

/**
 * @author Davood Akbari - 1404
 * daak1365@gmail.com
 * daak1365@yahoo.com
 * 09125188694
 */
public class PastOrPresentValidator implements AnnotationValidator<Object, PastOrPresent> {

    @Override
    public void validate(Object value, PastOrPresent annotation, String paramName) {
        if (value == null) {
            return;
        }

        boolean isAfterPresent = false;

        if (value instanceof Date) {
            isAfterPresent = ((Date) value).after(new Date());
        } else if (value instanceof LocalDate) {
            isAfterPresent = ((LocalDate) value).isAfter(LocalDate.now());
        } else if (value instanceof LocalDateTime) {
            isAfterPresent = ((LocalDateTime) value).isAfter(LocalDateTime.now());
        } else if (value instanceof Instant) {
            isAfterPresent = ((Instant) value).isAfter(Instant.now());
        } else if (value instanceof ChronoLocalDate) {
            isAfterPresent = ((ChronoLocalDate) value).isAfter(LocalDate.now());
        } else if (value instanceof ChronoLocalDateTime) {
            isAfterPresent = ((ChronoLocalDateTime<?>) value).isAfter(LocalDateTime.now());
        } else if (value instanceof ChronoZonedDateTime) {
            isAfterPresent = ((ChronoZonedDateTime<?>) value).toInstant().isAfter(Instant.now());
        } else if (value instanceof Long) {
            isAfterPresent = (Long) value > System.currentTimeMillis();
        }

        if (isAfterPresent) {
            throw DomainValidationException.withParams(MUST_BE_IN_PAST_OR_PRESENT, paramName, paramName);
        }
    }
}