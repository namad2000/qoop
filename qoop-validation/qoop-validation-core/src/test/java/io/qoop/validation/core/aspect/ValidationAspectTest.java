package io.qoop.validation.core.aspect;

import io.qoop.fault.handler.api.exception.DomainValidationException;
import io.qoop.validation.core.ValidationAspect;
import io.qoop.validation.core.configuration.ValidationConfig;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import static org.junit.jupiter.api.Assertions.*;

/**
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
        ValidationAspect.class
})
class ValidationAspectTest {

    @Autowired
    private TestValidationService service;

    @Test
    void testValidCase() {
        TestUser user = new TestUser("davood", new NestedInfo("admin"));

        assertDoesNotThrow(() -> {
            service.saveUser(user);
        });
    }

    @Test
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
    void testMethodNotExecutedOnError() {
        TestUser user = new TestUser("", new NestedInfo("admin"));

        assertThrows(
                DomainValidationException.class,
                () -> service.saveInvalidUser(user)
        );

        // If you want, you can use a spy to ensure method body never executed
    }

    @Test
    void testMethodNotExecutedOnErrorWithParam() {

        DomainValidationException ex = assertThrows(
                DomainValidationException.class,
                () -> service.dummyMethod(" ")
        );

        assertEquals("param", ex.getParamName());
        assertEquals("NotEmptyValidator-01", ex.getCode());
    }
}
