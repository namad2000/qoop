package io.qoop.validation.api.validator;

import io.qoop.fault.handler.api.exception.DomainValidationException;
import io.qoop.validation.api.ShetabCardNumber;

import static io.qoop.validation.api.exception.ValidationExceptionCode.INVALID_SHETAB_CARD_NUMBER;

/**
 * Validator implementation for {@link ShetabCardNumber} constraint.
 * Verifies Iranian Shetab 16-digit bank card numbers using the Modulo-10 (Luhn) algorithm.
 *
 * @author Davood Akbari - 1404
 * daak1365@gmail.com
 * daak1365@yahoo.com
 * 09125188694
 */
public class ShetabCardNumberValidator implements AnnotationValidator<CharSequence, ShetabCardNumber> {

    @Override
    public void validate(CharSequence value, ShetabCardNumber annotation, String paramName) {
        if (value == null || value.toString().trim().isEmpty()) {
            return;
        }

        String cardNumber = value.toString().replaceAll("[\\s-]", "");
        if (!isValidShetabCardNumber(cardNumber)) {
            throw DomainValidationException.withParams(INVALID_SHETAB_CARD_NUMBER, paramName, paramName);
        }
    }

    private boolean isValidShetabCardNumber(String cardNumber) {
        if (cardNumber == null || !cardNumber.matches("^\\d{16}$")) {
            return false;
        }

        int sum = 0;
        for (int i = 0; i < 16; i++) {
            int digit = cardNumber.charAt(i) - '0';
            // Multiply digits at even positions (0, 2, 4, ... 14) by 2
            int weight = (i % 2 == 0) ? 2 : 1;
            int subTotal = digit * weight;

            if (subTotal > 9) {
                subTotal -= 9;
            }
            sum += subTotal;
        }

        return (sum % 10 == 0);
    }
}