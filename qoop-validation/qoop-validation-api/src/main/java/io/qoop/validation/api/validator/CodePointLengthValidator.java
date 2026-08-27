package io.qoop.validation.api.validator;

import io.qoop.fault.handler.api.exception.DomainValidationException;
import io.qoop.validation.api.CodePointLength;

import static io.qoop.validation.api.exception.ValidationExceptionCode.INVALID_CODE_POINT_LENGTH;

/**
 * @author Davood Akbari - 1404
 * daak1365@gmail.com
 * daak1365@yahoo.com
 * 09125188694
 */
public class CodePointLengthValidator implements AnnotationValidator<CharSequence, CodePointLength> {

    @Override
    public void validate(CharSequence value, CodePointLength annotation, String paramName) {
        if (value == null) {
            return;
        }

        String strValue = value.toString();
        int codePointCount = strValue.codePointCount(0, strValue.length());

        if (codePointCount < annotation.min() || codePointCount > annotation.max()) {
            throw DomainValidationException.withParams(INVALID_CODE_POINT_LENGTH, paramName, paramName, annotation.min(), annotation.max());
        }
    }
}