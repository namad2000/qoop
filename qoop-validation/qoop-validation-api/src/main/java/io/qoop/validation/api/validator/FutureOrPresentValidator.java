package io.qoop.validation.api.validator;

import io.qoop.fault.handler.api.exception.DomainValidationException;
import io.qoop.validation.api.FutureOrPresent;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.chrono.ChronoLocalDate;
import java.time.chrono.ChronoLocalDateTime;
import java.time.chrono.ChronoZonedDateTime;
import java.util.Date;

import static io.qoop.validation.api.exception.ValidationExceptionCode.MUST_BE_IN_FUTURE_OR_PRESENT;

/**
 * @author Davood Akbari - 1404
 * daak1365@gmail.com
 * daak1365@yahoo.com
 * 09125188694
 */
public class FutureOrPresentValidator implements AnnotationValidator<Object, FutureOrPresent> {

    @Override
    public void validate(Object value, FutureOrPresent annotation, String paramName) {
        if (value == null) {
            return;
        }

        boolean isBeforePresent = false;

        if (value instanceof Date) {
            isBeforePresent = ((Date) value).before(new Date());
        } else if (value instanceof LocalDate) {
            isBeforePresent = ((LocalDate) value).isBefore(LocalDate.now());
        } else if (value instanceof LocalDateTime) {
            isBeforePresent = ((LocalDateTime) value).isBefore(LocalDateTime.now());
        } else if (value instanceof Instant) {
            isBeforePresent = ((Instant) value).isBefore(Instant.now());
        } else if (value instanceof ChronoLocalDate) {
            isBeforePresent = ((ChronoLocalDate) value).isBefore(LocalDate.now());
        } else if (value instanceof ChronoLocalDateTime) {
            isBeforePresent = ((ChronoLocalDateTime<?>) value).isBefore(LocalDateTime.now());
        } else if (value instanceof ChronoZonedDateTime) {
            isBeforePresent = ((ChronoZonedDateTime<?>) value).toInstant().isBefore(Instant.now());
        } else if (value instanceof Long) {
            isBeforePresent = (Long) value < System.currentTimeMillis();
        }

        if (isBeforePresent) {
            throw DomainValidationException.withParams(MUST_BE_IN_FUTURE_OR_PRESENT, paramName, paramName);
        }
    }
}