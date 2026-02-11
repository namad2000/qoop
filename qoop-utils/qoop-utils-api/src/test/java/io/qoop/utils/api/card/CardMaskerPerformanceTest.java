package io.qoop.utils.api.card;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;

import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

class CardMaskerPerformanceTest {

    @Test
    @Timeout(value = 100, unit = TimeUnit.MILLISECONDS)
    @DisplayName("Masking 1000 card numbers in less than 100 milliseconds")
    void maskCardNumber_PerformanceTest() {
        // Given
        String cardNumber = "6037991234567890";

        // When & Then
        for (int i = 0; i < 1000; i++) {
            String result = CardMasker.maskCardNumber(cardNumber + i);
            assertThat(result).isNotNull();
        }
    }

    @Test
    @DisplayName("Compare performance of two methods")
    void performanceComparison() {
        // Given
        String cardNumber = "6037-9912-3456-7890";
        int iterations = 10000;

        // Timing for the first method
        long startTime1 = System.nanoTime();
        for (int i = 0; i < iterations; i++) {
            CardMasker.maskCardNumber(cardNumber);
        }
        long duration1 = System.nanoTime() - startTime1;

        // Timing for the second method
        long startTime2 = System.nanoTime();
        for (int i = 0; i < iterations; i++) {
            CardMasker.maskCardNumberWithSpaces(cardNumber);
        }
        long duration2 = System.nanoTime() - startTime2;

        System.out.printf("maskCardNumber: %d ns per call%n", duration1 / iterations);
        System.out.printf("maskCardNumberWithSpaces: %d ns per call%n", duration2 / iterations);

        // Verify performance is acceptable
        assertThat(duration1).isLessThan(100_000_000); // less than 100 milliseconds for 10000 calls
    }
}
