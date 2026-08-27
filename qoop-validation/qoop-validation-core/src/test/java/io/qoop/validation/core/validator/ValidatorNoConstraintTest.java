package io.qoop.validation.core.validator;

import io.qoop.validation.core.Validator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.config.AutowireCapableBeanFactory;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

/**
 * Unit test verifying {@link Validator} behavior when target DTO contains fields without validation constraints.
 *
 * @author Davood Akbari - 1404
 */
class ValidatorNoConstraintTest {

    private Validator validator;

    @BeforeEach
    void setUp() {
        AutowireCapableBeanFactory beanFactory = Mockito.mock(AutowireCapableBeanFactory.class);
        validator = new Validator(beanFactory);
    }

    @Test
    @DisplayName("Should pass successfully when validating a DTO field with no constraint annotations")
    void testFieldWithoutConstraint() {
        TestDto dto = new TestDto("value");

        assertDoesNotThrow(() -> validator.validate(dto));
    }
}