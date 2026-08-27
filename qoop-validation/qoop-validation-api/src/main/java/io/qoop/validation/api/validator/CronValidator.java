package io.qoop.validation.api.validator;

import io.qoop.fault.handler.api.exception.DomainValidationException;
import io.qoop.validation.api.Cron;
import io.qoop.validation.api.interfaces.CronExpression;
import lombok.RequiredArgsConstructor;

import static io.qoop.validation.api.exception.ValidationExceptionCode.INVALID_CRON_EXPRESSION;

/**
 * Validator implementation for {@link Cron} constraint.
 * Utilizes {@link CronExpression} to validate cron expression syntax.
 *
 * @author Davood Akbari - 1404
 * daak1365@gmail.com
 * daak1365@yahoo.com
 * 09125188694
 */

@RequiredArgsConstructor
public class CronValidator implements AnnotationValidator<String, Cron> {

    private final CronExpression cronExpression;


    @Override
    public void validate(String value, Cron annotation, String paramName) {
        if (value != null && !cronExpression.isValidExpression(value)) {
            throw DomainValidationException.withParams(INVALID_CRON_EXPRESSION, paramName, paramName);
        }
    }
}