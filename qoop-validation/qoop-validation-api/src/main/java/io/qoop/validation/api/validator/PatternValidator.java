package io.qoop.validation.api.validator;

import io.qoop.fault.handler.api.exception.DomainValidationException;
import io.qoop.validation.api.Pattern;

import static io.qoop.validation.api.exception.ValidationExceptionCode.INVALID_PATTERN;

/**
 * @author Davood Akbari - 1404
 * daak1365@gmail.com
 * daak1365@yahoo.com
 * 09125188694
 */
public class PatternValidator implements AnnotationValidator<CharSequence, Pattern> {

    @Override
    public void validate(CharSequence value, Pattern annotation, String paramName) {
        if (value == null || value.length() == 0) {
            return;
        }

        String regexp = annotation.regexp();
        if (!value.toString().matches(regexp)) {
            throw DomainValidationException.withParams(INVALID_PATTERN, paramName, paramName, regexp);
        }
    }
}