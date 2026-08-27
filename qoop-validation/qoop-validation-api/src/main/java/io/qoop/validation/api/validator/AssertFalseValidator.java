package io.qoop.validation.api.validator;

import io.qoop.fault.handler.api.exception.DomainValidationException;
import io.qoop.validation.api.AssertFalse;

import static io.qoop.validation.api.exception.ValidationExceptionCode.MUST_BE_FALSE;

/**
 * @author Davood Akbari - 1404
 * daak1365@gmail.com
 * daak1365@yahoo.com
 * 09125188694
 */
public class AssertFalseValidator implements AnnotationValidator<Boolean, AssertFalse> {

    @Override
    public void validate(Boolean value, AssertFalse annotation, String paramName) {
        if (value == null) {
            return;
        }

        if (value) {
            throw DomainValidationException.withParams(MUST_BE_FALSE, paramName, paramName);
        }
    }
}