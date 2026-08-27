package io.qoop.validation.api.validator;

import io.qoop.fault.handler.api.exception.DomainValidationException;
import io.qoop.validation.api.EnumValue;

import java.util.Arrays;

import static io.qoop.validation.api.exception.ValidationExceptionCode.INVALID_ENUM_VALUE;

/**
 * @author Davood Akbari - 1404
 * daak1365@gmail.com
 * daak1365@yahoo.com
 * 09125188694
 */
public class EnumValueValidator implements AnnotationValidator<Object, EnumValue> {

    @Override
    public void validate(Object value, EnumValue annotation, String paramName) {
        if (value == null) {
            return;
        }

        String strValue = value.toString();
        Enum<?>[] enumConstants = annotation.enumClass().getEnumConstants();

        if (enumConstants == null) {
            throw DomainValidationException.withParams(INVALID_ENUM_VALUE, paramName, paramName);
        }

        boolean isValid = Arrays.stream(enumConstants)
                .anyMatch(e -> annotation.ignoreCase() 
                        ? e.name().equalsIgnoreCase(strValue) 
                        : e.name().equals(strValue));

        if (!isValid) {
            throw DomainValidationException.withParams(INVALID_ENUM_VALUE, paramName, paramName);
        }
    }
}