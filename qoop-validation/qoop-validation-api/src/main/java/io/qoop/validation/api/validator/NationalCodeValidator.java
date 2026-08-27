package io.qoop.validation.api.validator;

import io.qoop.fault.handler.api.exception.DomainValidationException;
import io.qoop.validation.api.NationalCode;

import static io.qoop.validation.api.exception.ValidationExceptionCode.INVALID_NATIONAL_CODE;

/**
 * @author Davood Akbari - 1404
 * daak1365@gmail.com
 * daak1365@yahoo.com
 * 09125188694
 */
public class NationalCodeValidator implements AnnotationValidator<CharSequence, NationalCode> {

    @Override
    public void validate(CharSequence value, NationalCode annotation, String paramName) {
        if (value == null || value.isEmpty()) {
            return;
        }

        String nationalCode = value.toString();
        if (!isValidNationalCode(nationalCode)) {
            throw DomainValidationException.withParams(INVALID_NATIONAL_CODE, paramName, paramName);
        }
    }

    private boolean isValidNationalCode(String code) {
        if (code == null || !code.matches("^\\d{10}$")) {
            return false;
        }

        if (code.matches("^(\\d)\\1{9}$")) {
            return false;
        }

        int sum = 0;
        for (int i = 0; i < 9; i++) {
            sum += (code.charAt(i) - '0') * (10 - i);
        }

        int remainder = sum % 11;
        int checkDigit = code.charAt(9) - '0';

        if (remainder < 2) {
            return checkDigit == remainder;
        } else {
            return checkDigit == (11 - remainder);
        }
    }
}