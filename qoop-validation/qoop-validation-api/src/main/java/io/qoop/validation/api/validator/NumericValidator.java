package io.qoop.validation.api.validator;

import io.qoop.fault.handler.api.exception.DomainValidationException;
import io.qoop.validation.api.Numeric;

import java.util.regex.Pattern;

import static io.qoop.validation.api.exception.ValidationExceptionCode.MUST_BE_NUMERIC;

/**
 * @author Davood Akbari - 1404
 * daak1365@gmail.com
 * daak1365@yahoo.com
 * 09125188694
 */
public class NumericValidator implements AnnotationValidator<CharSequence, Numeric> {

    private static final Pattern NUMERIC_PATTERN = Pattern.compile("^[0-9]+$");

    @Override
    public void validate(CharSequence value, Numeric annotation, String paramName) {
        if (value == null || value.isEmpty()) {
            return;
        }

        if (!NUMERIC_PATTERN.matcher(value).matches()) {
            throw DomainValidationException.withParams(MUST_BE_NUMERIC, paramName, paramName);
        }
    }
}