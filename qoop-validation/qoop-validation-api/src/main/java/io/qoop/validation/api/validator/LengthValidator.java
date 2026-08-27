package io.qoop.validation.api.validator;

import io.qoop.fault.handler.api.exception.DomainValidationException;
import io.qoop.validation.api.Length;

import static io.qoop.validation.api.exception.ValidationExceptionCode.INVALID_LENGTH;

/**
 * @author Davood Akbari - 1404
 * daak1365@gmail.com
 * daak1365@yahoo.com
 * 09125188694
 */
public class LengthValidator implements AnnotationValidator<CharSequence, Length> {

    @Override
    public void validate(CharSequence value, Length annotation, String paramName) {
        if (value == null) {
            return;
        }

        int length = value.length();
        if (length < annotation.min() || length > annotation.max()) {
            throw DomainValidationException.withParams(
                    INVALID_LENGTH,
                    paramName,
                    paramName,
                    annotation.min(),
                    annotation.max()
            );
        }
    }
}