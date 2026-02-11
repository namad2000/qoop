package io.qoop.validation.core.validator;

import io.qoop.validation.core.Validator;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

class ValidatorNoConstraintTest {

    @Test
    void testFieldWithoutConstraint() {
        TestDto dto = new TestDto("value");

        assertDoesNotThrow(() -> Validator.validate(dto));
    }
}
