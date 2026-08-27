package io.qoop.validation.api.validator;

import io.qoop.fault.handler.api.exception.DomainValidationException;
import io.qoop.validation.api.IranianIBAN;

import java.math.BigInteger;

import static io.qoop.validation.api.exception.ValidationExceptionCode.INVALID_IRANIAN_IBAN;

/**
 * Validates Iranian IBAN (Sheba) formats according to ISO 13616 / ISO 7064 Mod 97-10 standards.
 *
 * @author Davood Akbari - 1404
 * daak1365@gmail.com
 * daak1365@yahoo.com
 * 09125188694
 */
public class IranianIBANValidator implements AnnotationValidator<CharSequence, IranianIBAN> {

    @Override
    public void validate(CharSequence value, IranianIBAN annotation, String paramName) {
        if (value == null || value.toString().trim().isEmpty()) {
            return;
        }

        String iban = normalize(value.toString());

        if (!isValidIranianIBAN(iban)) {
            throw DomainValidationException.withParams(INVALID_IRANIAN_IBAN, paramName, paramName);
        }
    }

    private boolean isValidIranianIBAN(String iban) {
        if (iban == null || !iban.matches("^IR\\d{24}$")) {
            return false;
        }

        try {
            // Move first 4 characters (IRxx) to the end: IRxxBB...BB -> BB...BBIRxx
            String rearranged = iban.substring(4) + iban.substring(0, 4);

            StringBuilder numericBuilder = new StringBuilder();
            for (char ch : rearranged.toCharArray()) {
                if (Character.isLetter(ch)) {
                    // 'I' -> 18, 'R' -> 27
                    numericBuilder.append(ch - 'A' + 10);
                } else {
                    numericBuilder.append(ch);
                }
            }

            BigInteger numericIban = new BigInteger(numericBuilder.toString());
            return numericIban.mod(BigInteger.valueOf(97)).intValue() == 1;
        } catch (Exception e) {
            return false;
        }
    }

    private String normalize(String input) {
        if (input == null) {
            return "";
        }

        StringBuilder sb = new StringBuilder();
        for (char ch : input.toCharArray()) {
            if (Character.isWhitespace(ch)) {
                continue;
            }
            if (ch >= '۰' && ch <= '۹') { // Convert Persian numbers
                sb.append((char) (ch - '۰' + '0'));
            } else if (ch >= '٠' && ch <= '٩') { // Convert Arabic numbers
                sb.append((char) (ch - '٠' + '0'));
            } else {
                sb.append(Character.toUpperCase(ch));
            }
        }
        return sb.toString();
    }
}