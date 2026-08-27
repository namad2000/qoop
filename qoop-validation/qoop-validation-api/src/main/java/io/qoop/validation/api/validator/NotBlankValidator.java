package io.qoop.validation.api.validator;

import io.qoop.fault.handler.api.exception.DomainValidationException;
import io.qoop.validation.api.NotBlank;

import static io.qoop.validation.api.exception.ValidationExceptionCode.NOT_BLANK;

/**
 * @author Davood Akbari - 1404
 * daak1365@gmail.com
 * daak1365@yahoo.com
 * 09125188694
 */
public class NotBlankValidator implements AnnotationValidator<CharSequence, NotBlank> {

    @Override
    public void validate(CharSequence value, NotBlank annotation, String paramName) {
        if (value == null || value.toString().trim().isEmpty()) {
            throw DomainValidationException.withParams(NOT_BLANK, paramName, paramName);
        }
    }
}