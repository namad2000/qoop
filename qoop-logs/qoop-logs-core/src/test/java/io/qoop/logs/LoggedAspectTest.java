package io.qoop.logs;

import io.qoop.logs.annotation.Logged;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.reflect.MethodSignature;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import tools.jackson.databind.ObjectMapper;

import java.lang.reflect.Method;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Author: davood akbari
 * Email: daak1365@gmail.com
 * Created: 8/18/2026 1:46 PM
 * Package: io.qoop.logs
 */


@ExtendWith(MockitoExtension.class)
class LoggedAspectTest {

    @Mock
    private DomainLogger domainLogger;

    @Mock
    private ObjectMapper objectMapper;

    @Mock
    private ProceedingJoinPoint joinPoint;

    @Mock
    private MethodSignature methodSignature;

    private LoggedAspect loggedAspect;

    @BeforeEach
    void setUp() {
        loggedAspect = new LoggedAspect(domainLogger, objectMapper);
    }

    @Test
    @DisplayName("Should serialize arguments and return value to JSON upon successful execution")
    void shouldSerializeArgsAndResultToJsonOnSuccess() throws Throwable {
        TestTarget target = new TestTarget();
        Method method = TestTarget.class.getMethod("annotatedMethod", TestUser.class);
        TestUser user = new TestUser("101", "davood");
        Object[] args = new Object[]{user};

        when(joinPoint.getTarget()).thenReturn(target);
        when(joinPoint.getSignature()).thenReturn(methodSignature);
        when(methodSignature.getMethod()).thenReturn(method);
        when(joinPoint.getArgs()).thenReturn(args);
        when(joinPoint.proceed()).thenReturn("SUCCESS");

        when(objectMapper.writeValueAsString(user)).thenReturn("{\"id\":\"101\",\"name\":\"davood\"}");
        when(objectMapper.writeValueAsString("SUCCESS")).thenReturn("\"SUCCESS\"");

        Object result = loggedAspect.logExecution(joinPoint);

        assertEquals("SUCCESS", result);

        // Verify entry log with JSON serialized arguments
        verify(domainLogger).logInfoForClass(
                eq(TestTarget.class),
                eq("BANK_KEY"),
                eq("Entering method: {} | Arguments: {}"),
                eq("annotatedMethod"),
                eq("[{\"id\":\"101\",\"name\":\"davood\"}]")
        );

        // Verify exit log with JSON serialized return result
        verify(domainLogger).logInfoForClass(
                eq(TestTarget.class),
                eq("BANK_KEY"),
                eq("Exiting method: {} | Result: {}"),
                eq("annotatedMethod"),
                eq("\"SUCCESS\"")
        );
    }

    @Test
    @DisplayName("Should fallback to toString() when JSON serialization throws an exception")
    void shouldFallbackToStringWhenSerializationFails() throws Throwable {
        TestTarget target = new TestTarget();
        Method method = TestTarget.class.getMethod("annotatedMethod", TestUser.class);
        TestUser user = new TestUser("101", "davood");
        Object[] args = new Object[]{user};

        when(joinPoint.getTarget()).thenReturn(target);
        when(joinPoint.getSignature()).thenReturn(methodSignature);
        when(methodSignature.getMethod()).thenReturn(method);
        when(joinPoint.getArgs()).thenReturn(args);
        when(joinPoint.proceed()).thenReturn("SUCCESS");

        // Mocking general RuntimeException instead of JsonProcessingException
        when(objectMapper.writeValueAsString(any())).thenThrow(new RuntimeException("General serialization error"));

        loggedAspect.logExecution(joinPoint);

        // Verify entry log used fallback toString() representation
        verify(domainLogger).logInfoForClass(
                eq(TestTarget.class),
                eq("BANK_KEY"),
                eq("Entering method: {} | Arguments: {}"),
                eq("annotatedMethod"),
                eq("[" + user.toString() + "]")
        );
    }

    @Test
    @DisplayName("Should handle null arguments and null return value correctly")
    void shouldHandleNullArgumentsAndNullResult() throws Throwable {
        TestTarget target = new TestTarget();
        Method method = TestTarget.class.getMethod("annotatedMethod", TestUser.class);
        Object[] args = new Object[]{null};

        when(joinPoint.getTarget()).thenReturn(target);
        when(joinPoint.getSignature()).thenReturn(methodSignature);
        when(methodSignature.getMethod()).thenReturn(method);
        when(joinPoint.getArgs()).thenReturn(args);
        when(joinPoint.proceed()).thenReturn(null);

        loggedAspect.logExecution(joinPoint);

        // Verify entry log handles null argument
        verify(domainLogger).logInfoForClass(
                eq(TestTarget.class),
                eq("BANK_KEY"),
                eq("Entering method: {} | Arguments: {}"),
                eq("annotatedMethod"),
                eq("[null]")
        );

        // Verify exit log handles null result
        verify(domainLogger).logInfoForClass(
                eq(TestTarget.class),
                eq("BANK_KEY"),
                eq("Exiting method: {} | Result: {}"),
                eq("annotatedMethod"),
                eq("null")
        );
    }

    @Test
    @DisplayName("Should log exception message when target method throws an exception")
    void shouldLogExceptionDetailsOnFailure() throws Throwable {
        TestTarget target = new TestTarget();
        Method method = TestTarget.class.getMethod("annotatedMethod", TestUser.class);
        Object[] args = new Object[]{};
        RuntimeException exception = new RuntimeException("Database connection timed out");

        when(joinPoint.getTarget()).thenReturn(target);
        when(joinPoint.getSignature()).thenReturn(methodSignature);
        when(methodSignature.getMethod()).thenReturn(method);
        when(joinPoint.getArgs()).thenReturn(args);
        when(joinPoint.proceed()).thenThrow(exception);

        assertThrows(RuntimeException.class, () -> loggedAspect.logExecution(joinPoint));

        // Verify exception log
        verify(domainLogger).logInfoForClass(
                eq(TestTarget.class),
                eq("BANK_KEY"),
                eq("Exception in method: {} | Message: {}"),
                eq("annotatedMethod"),
                eq("Database connection timed out")
        );
    }

    // =========================================================================
    // Test Dummy Helper Classes
    // =========================================================================

    static class TestTarget {
        @Logged("BANK_KEY")
        public String annotatedMethod(TestUser user) {
            return "SUCCESS";
        }
    }

    static class TestUser {
        private final String id;
        private final String name;

        public TestUser(String id, String name) {
            this.id = id;
            this.name = name;
        }

        @Override
        public String toString() {
            return "TestUser{id='" + id + "', name='" + name + "'}";
        }
    }
}