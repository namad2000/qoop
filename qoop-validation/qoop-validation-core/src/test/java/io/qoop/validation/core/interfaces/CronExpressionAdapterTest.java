package io.qoop.validation.core.interfaces;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class CronExpressionAdapterTest {

    private CronExpressionAdapter adapter;

    @BeforeEach
    void setUp() {
        adapter = new CronExpressionAdapter();
    }

    @Test
    void isValidExpression_WithValidCron_ShouldReturnTrue() {
        // A standard 6-field Spring cron expression
        String validCron = "0 0 12 * * ?";

        boolean result = adapter.isValidExpression(validCron);

        assertTrue(result);
    }

    @Test
    void isValidExpression_WithInvalidCron_ShouldReturnFalse() {
        String invalidCron = "invalid cron expression";

        boolean result = adapter.isValidExpression(invalidCron);

        assertFalse(result);
    }

    @ParameterizedTest
    @ValueSource(strings = {"", "   "})
    void isValidExpression_WithEmptyOrBlankString_ShouldReturnFalse(String emptyCron) {
        boolean result = adapter.isValidExpression(emptyCron);

        assertFalse(result);
    }

    @Test
    void isValidExpression_WithNull_ShouldReturnFalse() {
        boolean result = adapter.isValidExpression(null);

        assertFalse(result);
    }

    @Test
    void isValidExpression_WithValidCronAndSpaces_ShouldTrimAndReturnTrue() {
        // Check whitespace handling (trimming)
        String cronWithSpaces = "  0 0 12 * * ?  ";

        boolean result = adapter.isValidExpression(cronWithSpaces);

        assertTrue(result);
    }
}