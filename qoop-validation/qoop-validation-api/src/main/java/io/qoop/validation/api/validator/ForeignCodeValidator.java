package io.qoop.validation.api.validator;

import io.qoop.fault.handler.api.exception.DomainValidationException;
import io.qoop.validation.api.ForeignCode;

import static io.qoop.validation.api.exception.ValidationExceptionCode.INVALID_FOREIGN_CODE;

/**
 * @author Davood Akbari - 1404
 * daak1365@gmail.com
 * daak1365@yahoo.com
 * 09125188694
 */
public class ForeignCodeValidator implements AnnotationValidator<CharSequence, ForeignCode> {

    private static final int[] COEFFICIENTS = {3, 7, 1, 3, 7, 1, 3, 7, 1, 3, 7, 1};

    @Override
    public void validate(CharSequence value, ForeignCode annotation, String paramName) {
        if (value == null || value.length() == 0) {
            return;
        }

        String foreignCode = value.toString();
        if (!isValidForeignCode(foreignCode)) {
            throw DomainValidationException.withParams(INVALID_FOREIGN_CODE, paramName, paramName);
        }
    }

    private boolean isValidForeignCode(String code) {
        if (code == null || !code.matches("^\\d{13}$")) {
            return false;
        }

        int sum = 0;
        for (int i = 0; i < 12; i++) {
            sum += (code.charAt(i) - '0') * COEFFICIENTS[i];
        }

        int remainder = sum % 10;
        int checkDigit = (10 - remainder) % 10;

        return (code.charAt(12) - '0') == checkDigit;
    }
}