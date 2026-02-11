package io.qoop.validation.core.validator;

import io.qoop.fault.handler.api.exception.DomainValidationException;
import io.qoop.validation.api.NotEmpty;
import io.qoop.validation.core.Validator;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Parameter;

public class ValidatorTest {

    @Test
    void testValidSimpleField() {
        UserTestModel model = new UserTestModel("davood", new NestedInfo("admin"));

        Assertions.assertDoesNotThrow(() -> Validator.validate(model));
    }

    @Test
    void testInvalidSimpleField() {
        UserTestModel model = new UserTestModel("   ", new NestedInfo("admin"));

        DomainValidationException ex = Assertions.assertThrows(
                DomainValidationException.class,
                () -> Validator.validate(model)
        );

        Assertions.assertEquals("username", ex.getParamName());
        Assertions.assertEquals("NotEmptyValidator-01", ex.getCode());
    }

    @Test
    void testNestedValidationValid() {
        UserTestModel model = new UserTestModel("davood", new NestedInfo("test"));

        Assertions.assertDoesNotThrow(() -> {
            Validator.validate(model);
        });
    }

    @Test
    void testNestedValidationInvalid() {
        UserTestModel model = new UserTestModel("davood", new NestedInfo(""));

        DomainValidationException ex = Assertions.assertThrows(
                DomainValidationException.class,
                () -> Validator.validate(model)
        );

        Assertions.assertEquals("title", ex.getParamName());
        Assertions.assertEquals("NotEmptyValidator-01", ex.getCode());
    }

    @Test
    void testMethodParamValidationValid() throws Exception {
        Object[] args = new Object[]{"hello"};
        Parameter[] params = DummyClass.class.getMethod("dummyMethod", String.class).getParameters();

        Assertions.assertDoesNotThrow(() -> {
            Validator.validateMethodParams(args, params);
        });
    }

    @Test
    void testMethodParamValidationInvalid() throws Exception {
        Object[] args = new Object[]{""};
        Parameter[] params = DummyClass.class.getMethod("dummyMethod", String.class).getParameters();

        DomainValidationException ex = Assertions.assertThrows(
                DomainValidationException.class,
                () -> Validator.validateMethodParams(args, params)
        );

        Assertions.assertEquals("param", ex.getParamName());
        Assertions.assertEquals("NotEmptyValidator-01", ex.getCode());
    }

    static class DummyClass {
        public void dummyMethod(@NotEmpty String param) {
        }
    }
}
