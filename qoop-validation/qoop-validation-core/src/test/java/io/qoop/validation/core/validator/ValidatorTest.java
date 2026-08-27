package io.qoop.validation.core.validator;

import io.qoop.fault.handler.api.exception.DomainValidationException;
import io.qoop.validation.api.Constraint;
import io.qoop.validation.api.NotEmpty;
import io.qoop.validation.api.ValidatedBy;
import io.qoop.validation.api.validator.AnnotationValidator;
import io.qoop.validation.api.validator.NotEmptyValidator;
import io.qoop.validation.core.Validator;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.config.AutowireCapableBeanFactory;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.lang.reflect.Parameter;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

/**
 * Unit tests for {@link Validator} verifying bean injection, field, and method validation features.
 *
 * @author Davood Akbari - 1404
 */
public class ValidatorTest {

    private Validator validator;
    private AutowireCapableBeanFactory beanFactory;

    @BeforeEach
    void setUp() {
        beanFactory = Mockito.mock(AutowireCapableBeanFactory.class);

        // Fallback behavior: dynamically instantiate requested validator instances when beanFactory is invoked
        Mockito.when(beanFactory.createBean(any())).thenAnswer(invocation -> {
            Class<?> clazz = invocation.getArgument(0);
            return clazz.getDeclaredConstructor().newInstance();
        });

        validator = new Validator(beanFactory);
    }

    @Test
    @DisplayName("Should pass when simple target model contains valid non-empty fields")
    void testValidSimpleField() {
        UserTestModel model = new UserTestModel("davood", new NestedInfo("admin"));

        Assertions.assertDoesNotThrow(() -> validator.validate(model));
    }

    @Test
    @DisplayName("Should throw DomainValidationException when a simple field fails validation")
    void testInvalidSimpleField() {
        UserTestModel model = new UserTestModel("   ", new NestedInfo("admin"));

        DomainValidationException ex = Assertions.assertThrows(
                DomainValidationException.class,
                () -> validator.validate(model)
        );

        Assertions.assertEquals("username", ex.getParamName());
        Assertions.assertEquals("NotEmptyValidator-01", ex.getCode());
    }

    @Test
    @DisplayName("Should pass nested validation when child object fields are valid")
    void testNestedValidationValid() {
        UserTestModel model = new UserTestModel("davood", new NestedInfo("test"));

        Assertions.assertDoesNotThrow(() -> validator.validate(model));
    }

    @Test
    @DisplayName("Should throw DomainValidationException when a nested object field is invalid")
    void testNestedValidationInvalid() {
        UserTestModel model = new UserTestModel("davood", new NestedInfo(""));

        DomainValidationException ex = Assertions.assertThrows(
                DomainValidationException.class,
                () -> validator.validate(model)
        );

        Assertions.assertEquals("title", ex.getParamName());
        Assertions.assertEquals("NotEmptyValidator-01", ex.getCode());
    }

    @Test
    @DisplayName("Should pass method argument validation when input parameter is valid")
    void testMethodParamValidationValid() throws Exception {
        Object[] args = new Object[]{"hello"};
        Parameter[] params = DummyClass.class.getMethod("dummyMethod", String.class).getParameters();

        Assertions.assertDoesNotThrow(() -> validator.validateMethodParams(args, params));
    }

    @Test
    @DisplayName("Should throw DomainValidationException when method argument fails validation")
    void testMethodParamValidationInvalid() throws Exception {
        Object[] args = new Object[]{""};
        Parameter[] params = DummyClass.class.getMethod("dummyMethod", String.class).getParameters();

        DomainValidationException ex = Assertions.assertThrows(
                DomainValidationException.class,
                () -> validator.validateMethodParams(args, params)
        );

        Assertions.assertEquals("param", ex.getParamName());
        Assertions.assertEquals("NotEmptyValidator-01", ex.getCode());
    }

    @Test
    @DisplayName("Should verify Spring beanFactory integration for dynamic validator creation")
    void testSpringBeanDependencyInjection() {
        TestDependencyModel model = new TestDependencyModel("valid_data");

        Assertions.assertDoesNotThrow(() -> validator.validate(model));
        verify(beanFactory, times(1)).createBean(MockDependencyValidator.class);
    }

    @Test
    @DisplayName("Should use validator cache and create validator instance only once")
    void testValidatorCachingBehavior() {
        UserTestModel model1 = new UserTestModel("davood1", new NestedInfo("admin1"));
        UserTestModel model2 = new UserTestModel("davood2", new NestedInfo("admin2"));

        validator.validate(model1);
        validator.validate(model2);

        // Verify bean creation happens only once per validator class type due to internal cache
        verify(beanFactory, times(1)).createBean(NotEmptyValidator.class);
    }

    // Dummy elements supporting test cases
    static class DummyClass {
        public void dummyMethod(@NotEmpty String param) {
        }
    }

    static class TestDependencyModel {
        @CustomConstraint
        private final String data;

        public TestDependencyModel(String data) {
            this.data = data;
        }
    }

    @Target(ElementType.FIELD)
    @Retention(RetentionPolicy.RUNTIME)
    @Constraint
    @ValidatedBy(MockDependencyValidator.class)
    @interface CustomConstraint {
    }

    public static class MockDependencyValidator implements AnnotationValidator<String, CustomConstraint> {
        @Override
        public void validate(String value, CustomConstraint annotation, String paramName) {
            // Validation logic executed via Spring-managed bean lifecycle
        }
    }
}