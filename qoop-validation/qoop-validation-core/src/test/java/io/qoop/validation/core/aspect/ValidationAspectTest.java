package io.qoop.validation.core.aspect;

import io.qoop.fault.handler.api.exception.DomainValidationException;
import io.qoop.validation.core.ValidationAspect;
import io.qoop.validation.core.Validator;
import io.qoop.validation.core.configuration.ValidationConfig;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.bean.override.mockito.MockitoSpyBean;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

/**
 * Integration tests for {@link ValidationAspect} verifying AOP advice behavior on services.
 *
 * @author Davood Akbari - 1404
 * daak1365@gmail.com
 * daak1365@yahoo.com
 * 09125188694
 */
@SpringBootTest
@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = {
        TestValidationService.class,
        ValidationConfig.class,
        Validator.class,
        ValidationAspect.class
})
class ValidationAspectTest {

    @MockitoSpyBean
    private TestValidationService service;

    @Test
    @DisplayName("Should execute target service method without exception when request data is valid")
    void testValidCase() {
        TestUser user = new TestUser("davood", new NestedInfo("admin"));

        assertDoesNotThrow(() -> service.saveUser(user));
        verify(service).saveUser(user);
    }

    @Test
    @DisplayName("Should intercept execution and throw DomainValidationException for invalid root model field")
    void testInvalidField() {
        TestUser user = new TestUser("", new NestedInfo("admin"));

        DomainValidationException ex = assertThrows(
                DomainValidationException.class,
                () -> service.saveInvalidUser(user)
        );

        assertEquals("name", ex.getParamName());
        assertEquals("NotEmptyValidator-01", ex.getCode());
    }

    @Test
    @DisplayName("Should intercept execution and throw DomainValidationException for invalid nested object field")
    void testNestedInvalidField() {
        TestUser user = new TestUser("davood", new NestedInfo(""));

        DomainValidationException ex = assertThrows(
                DomainValidationException.class,
                () -> service.saveInvalidUser(user)
        );

        assertEquals("title", ex.getParamName());
        assertEquals("NotEmptyValidator-01", ex.getCode());
    }

    @Test
    @DisplayName("Should prevent service method body execution when root validation fails")
    void testMethodNotExecutedOnError() {
        TestUser user = new TestUser("", new NestedInfo("admin"));

        assertThrows(
                DomainValidationException.class,
                () -> service.saveInvalidUser(user)
        );

        // Verify that the actual target method logic was intercepted and never invoked
        verify(service, never()).saveInvalidUser(user);
    }

    @Test
    @DisplayName("Should prevent method body execution and raise exception on invalid parameter input")
    void testMethodNotExecutedOnErrorWithParam() {
        DomainValidationException ex = assertThrows(
                DomainValidationException.class,
                () -> service.dummyMethod(" ")
        );

        assertEquals("param", ex.getParamName());
        assertEquals("NotEmptyValidator-01", ex.getCode());
        verify(service, never()).dummyMethod(" ");
    }

    @Test
    @DisplayName("Should handle null arguments gracefully without throwing unexpected NullPointerException")
    void testNullArgumentHandling() {
        assertDoesNotThrow(() -> service.saveUser(null));
    }
}