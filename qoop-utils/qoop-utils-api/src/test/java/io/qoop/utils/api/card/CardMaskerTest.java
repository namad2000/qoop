package io.qoop.utils.api.card;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;

import static org.assertj.core.api.Assertions.assertThat;

class CardMaskerTest {

    @Test
    @DisplayName("Masking a 16-digit card number")
    void testMaskCardNumber_16Digits() {
        // Given
        String cardNumber = "6037991234567890";
        // When
        String result = CardMasker.maskCardNumber(cardNumber);
        // Then
        assertThat(result).isEqualTo("6037********7890");
    }

    @Test
    @DisplayName("Masking with spaces")
    void testMaskCardNumberWithSpaces() {
        // Given
        String cardNumber = "6037991234567890";
        // When
        String result = CardMasker.maskCardNumberWithSpaces(cardNumber);
        // Then
        assertThat(result).isEqualTo("6037 **** **** 7890");
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {" ", "  "})
    @DisplayName("Masking null or empty inputs")
    void testMaskNullOrEmpty(String input) {
        // When
        String result = CardMasker.maskCardNumber(input);
        // Then
        assertThat(result).isNull();
    }

    @Test
    @DisplayName("Masking American Express")
    void testMaskAmex() {
        // Given
        String cardNumber = "378282246310005";
        // When
        String result = CardMasker.maskCardNumber(cardNumber);
        // Then
        assertThat(result).isEqualTo("3782*******0005");
    }
}