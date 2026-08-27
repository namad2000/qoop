package io.qoop.validation.api.validator;

import io.qoop.fault.handler.api.exception.DomainValidationException;
import io.qoop.validation.api.IranianIBAN;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;

import java.math.BigInteger;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * Unit test for {@link IranianIBANValidator}.
 *
 * @author Davood Akbari - 1404
 * daak1365@gmail.com
 * daak1365@yahoo.com
 * 09125188694
 */

class IranianIBANValidatorTest {

    private IranianIBANValidator validator;
    private IranianIBAN annotation;

    @BeforeEach
    void setUp() {
        validator = new IranianIBANValidator();
        annotation = new IranianIBAN() {
            @Override
            public Class<? extends java.lang.annotation.Annotation> annotationType() {
                return IranianIBAN.class;
            }
        };
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {"   ", "\t", "\n"})
    @DisplayName("Should pass validation when input is null, empty or blank")
    void validate_NullOrEmpty_Success(String input) {
        assertDoesNotThrow(() -> validator.validate(input, annotation, "iban"));
    }

    @ParameterizedTest
    @ValueSource(strings = {
            "STANDARD",
            "LOWERCASE",
            "SPACES",
            "SPACED_FORMAT",
            "PERSIAN_DIGITS"
    })
    @DisplayName("Should pass validation when IBAN is valid")
    void validate_ValidIBAN_Success(String formatType) {
        // Generate a mathematically 100% valid IBAN based on exact validator logic
        String validIban = generateValidIBAN("0170000000001002345678");

        String testInput = switch (formatType) {
            case "STANDARD" -> validIban;
            case "LOWERCASE" -> validIban.toLowerCase();
            case "SPACES" -> " " + validIban + " ";
            case "SPACED_FORMAT" -> validIban.replaceAll("(.{4})", "$0 ").trim();
            case "PERSIAN_DIGITS" -> toPersianDigits(validIban);
            default -> validIban;
        };

        assertDoesNotThrow(() -> validator.validate(testInput, annotation, "iban"));
    }

    @ParameterizedTest
    @ValueSource(strings = {
            "IR000170000000001002345678", // Invalid check digit
            "IR12345678901234567890123456", // Invalid checksum format
            "US190170000000001002345678", // Invalid country code (not IR)
            "IR19017000000000100234567",  // Length too short
            "IR1901700000000010023456789", // Length too long
            "IR19017000000000100234567A", // Contains letter inside account number
            "1234567890123456789012345678" // No country prefix
    })
    @DisplayName("Should throw DomainValidationException when IBAN is invalid")
    void validate_InvalidIBAN_ThrowsException(String invalidIban) {
        assertThrows(DomainValidationException.class,
                () -> validator.validate(invalidIban, annotation, "iban"));
    }

    private String generateValidIBAN(String bban) {
        String dummyIban = "IR00" + bban;
        // Mirroring validator logic: move first 4 chars to end and convert letters
        String rearranged = dummyIban.substring(4) + dummyIban.substring(0, 4);
        StringBuilder numericBuilder = new StringBuilder();
        for (char ch : rearranged.toCharArray()) {
            if (Character.isLetter(ch)) {
                numericBuilder.append(ch - 'A' + 10);
            } else {
                numericBuilder.append(ch);
            }
        }
        BigInteger numericIban = new BigInteger(numericBuilder.toString());
        int remainder = numericIban.mod(BigInteger.valueOf(97)).intValue();
        int checkDigit = 98 - remainder;
        return "IR" + String.format("%02d", checkDigit) + bban;
    }

    private String toPersianDigits(String input) {
        char[] persianDigits = {'۰', '۱', '۲', '۳', '۴', '۵', '۶', '۷', '۸', '۹'};
        StringBuilder sb = new StringBuilder();
        for (char ch : input.toCharArray()) {
            if (ch >= '0' && ch <= '9') {
                sb.append(persianDigits[ch - '0']);
            } else {
                sb.append(ch);
            }
        }
        return sb.toString();
    }
}