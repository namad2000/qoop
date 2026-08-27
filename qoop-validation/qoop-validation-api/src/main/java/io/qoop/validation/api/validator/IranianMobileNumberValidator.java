package io.qoop.validation.api.validator;

import io.qoop.fault.handler.api.exception.DomainValidationException;

import java.util.regex.Pattern;

import static io.qoop.validation.api.exception.ValidationExceptionCode.INVALID_IRANIAN_MOBILE_NUMBER;

/**
 * @author Davood Akbari - 1404
 * daak1365@gmail.com
 * daak1365@yahoo.com
 * 09125188694
 */
public class IranianMobileNumberValidator implements AnnotationValidator<CharSequence, IranianMobileNumber> {

    private static final Pattern IRANIAN_MOBILE_PATTERN = Pattern.compile("^(\\+98|0098|0)?9\\d{9}$");

    @Override
    public void validate(CharSequence value, IranianMobileNumber annotation, String paramName) {
        if (value == null || value.isEmpty()) {
            return;
        }

        String mobileNumber = value.toString().trim();
        if (!IRANIAN_MOBILE_PATTERN.matcher(mobileNumber).matches()) {
            throw DomainValidationException.withParams(INVALID_IRANIAN_MOBILE_NUMBER, paramName, paramName);
        }
    }
}