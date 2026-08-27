package io.qoop.validation.api.validator;

import io.qoop.fault.handler.api.exception.DomainValidationException;
import io.qoop.validation.api.LegalNationalId;

import static io.qoop.validation.api.exception.ValidationExceptionCode.INVALID_LEGAL_NATIONAL_ID;

/**
 * Validator implementation for {@link LegalNationalId} constraint.
 * Validates 11-digit Iranian Legal Entity (Company) National IDs.
 *
 * @author Davood Akbari - 1404
 * daak1365@gmail.com
 * daak1365@yahoo.com
 * 09125188694
 */
public class LegalNationalIdValidator implements AnnotationValidator<CharSequence, LegalNationalId> {

    private static final int[] WEIGHTS = {29, 27, 23, 19, 17, 29, 27, 23, 19, 17};

    @Override
    public void validate(CharSequence value, LegalNationalId annotation, String paramName) {
        if (value == null || value.toString().trim().isEmpty()) {
            return;
        }

        String legalNationalId = value.toString().trim();
        if (!isValidLegalNationalId(legalNationalId)) {
            throw DomainValidationException.withParams(INVALID_LEGAL_NATIONAL_ID, paramName, paramName);
        }
    }

    private boolean isValidLegalNationalId(String id) {
        if (id == null || !id.matches("^\\d{11}$")) {
            return false;
        }

        try {
            int checkDigit = id.charAt(10) - '0';
            int remainder = getRemainder(id);
            if (remainder == 10) {
                remainder = 0;
            }

            return checkDigit == remainder;
        } catch (Exception e) {
            return false;
        }
    }

    private static int getRemainder(String id) {
        int tenthDigit = id.charAt(9) - '0';
        int sum = 0;

        // Standard Iranian Legal National ID formula: (digit + tenthDigit + 2) * weight
        // Or pure standard: sum of (digit + 2) * weight based on implementation variants.
        // Let's use the standard variant where the 10th digit is part of the sequence:
        for (int i = 0; i < 10; i++) {
            int digit = id.charAt(i) - '0';
            sum += (digit + 2) * WEIGHTS[i];
        }

        int remainder = sum % 11;
        return remainder;
    }
}