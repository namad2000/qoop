package io.qoop.validation.api.validator;

import io.qoop.fault.handler.api.exception.DomainValidationException;
import io.qoop.validation.api.IranianPostalCode;

import java.util.regex.Pattern;

import static io.qoop.validation.api.exception.ValidationExceptionCode.INVALID_IRANIAN_POSTAL_CODE;

/**
 * @author Davood Akbari - 1404
 * daak1365@gmail.com
 * daak1365@yahoo.com
 * 09125188694
 */
public class IranianPostalCodeValidator implements AnnotationValidator<CharSequence, IranianPostalCode> {

    private static final Pattern IRANIAN_POSTAL_CODE_PATTERN = Pattern.compile("^(?!.*[02]{5})[13-9]{5}[0-9]{5}$");

    @Override
    public void validate(CharSequence value, IranianPostalCode annotation, String paramName) {
        if (value == null || value.length() == 0) {
            return;
        }

        String postalCode = value.toString().trim();
        if (!IRANIAN_POSTAL_CODE_PATTERN.matcher(postalCode).matches()) {
            throw DomainValidationException.withParams(INVALID_IRANIAN_POSTAL_CODE, paramName, paramName);
        }
    }
}