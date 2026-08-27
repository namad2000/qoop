package io.qoop.validation.api.validator;

import io.qoop.fault.handler.api.exception.DomainValidationException;
import io.qoop.validation.api.Cron;
import io.qoop.validation.api.interfaces.CronExpression;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Unit tests for {@link CronValidator}.
 *
 * @author Davood Akbari - 1404
 * daak1365@gmail.com
 * daak1365@yahoo.com
 * 09125188694
 */
@ExtendWith(MockitoExtension.class)
class CronValidatorTest {

    @Mock
    private CronExpression cronExpression;

    @Mock
    private Cron cronAnnotation;

    private CronValidator cronValidator;

    @BeforeEach
    void setUp() {
        cronValidator = new CronValidator(cronExpression);
    }

    @Test
    @DisplayName("Should pass validation when cron expression is valid")
    void testValidateSuccess() {
        String validCron = "0 0 * * * *";
        String paramName = "cronField";

        when(cronExpression.isValidExpression(validCron)).thenReturn(true);

        assertDoesNotThrow(() -> cronValidator.validate(validCron, cronAnnotation, paramName));
        verify(cronExpression).isValidExpression(validCron);
    }

    @Test
    @DisplayName("Should throw DomainValidationException when cron expression is invalid")
    void testValidateInvalidCron() {
        String invalidCron = "invalid_cron";
        String paramName = "cronField";

        when(cronExpression.isValidExpression(invalidCron)).thenReturn(false);

        assertThrows(
                DomainValidationException.class,
                () -> cronValidator.validate(invalidCron, cronAnnotation, paramName)
        );
        verify(cronExpression).isValidExpression(invalidCron);
    }

    @Test
    @DisplayName("Should skip validation when input value is null")
    void testValidateNullValue() {
        String paramName = "cronField";

        assertDoesNotThrow(() -> cronValidator.validate(null, cronAnnotation, paramName));
        verify(cronExpression, Mockito.never()).isValidExpression(Mockito.anyString());
    }
}