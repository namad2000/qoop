package io.qoop.transaction.core;

import io.qoop.transaction.api.DomainTransactional;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.reflect.MethodSignature;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.DefaultTransactionDefinition;

import java.lang.reflect.Method;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class DomainTransactionalAspectTest {

    @Mock
    PlatformTransactionManager transactionManager;

    @Mock
    ProceedingJoinPoint joinPoint;

    @Mock
    MethodSignature methodSignature;

    @Mock
    TransactionStatus txStatus;

    @Captor
    ArgumentCaptor<DefaultTransactionDefinition> defCaptor;

    private DomainTransactionAspect aspect;

    // Dummy classes for reflection mocking in tests
    static class TestService {
        @DomainTransactional(value = DomainTransactional.TxType.REQUIRES_NEW)
        public void methodRequiresNew() {}

        @DomainTransactional(value = DomainTransactional.TxType.SUPPORTS)
        public void methodSupports() {}

        @DomainTransactional(value = DomainTransactional.TxType.REQUIRED)
        public void methodRequired() {}

        @DomainTransactional(
                rollbackOn = {MyCheckedException.class},
                dontRollbackOn = {MyCustomRuntimeException.class}
        )
        public void methodCustomExceptions() {}
    }

    static class MyCustomRuntimeException extends RuntimeException {}
    static class MyCheckedException extends Exception {}

    @BeforeEach
    void setUp() {
        aspect = new DomainTransactionAspect(transactionManager);

        when(transactionManager.getTransaction(any(TransactionDefinition.class)))
                .thenReturn(txStatus);

        when(joinPoint.getSignature()).thenReturn(methodSignature);
    }

    private void mockMethod(String methodName) throws NoSuchMethodException {
        Method method = TestService.class.getMethod(methodName);
        when(methodSignature.getMethod()).thenReturn(method);
    }

    // ---------------- Propagation-related tests ----------------

    @Test
    void should_setPropagation_requiresNew_when_annotation_requiresNew() throws Throwable {
        mockMethod("methodRequiresNew");
        when(joinPoint.proceed()).thenReturn("OK");

        Object result = aspect.handleTransaction(joinPoint);

        assertEquals("OK", result);

        verify(transactionManager).getTransaction(defCaptor.capture());
        assertEquals(
                TransactionDefinition.PROPAGATION_REQUIRES_NEW,
                defCaptor.getValue().getPropagationBehavior()
        );

        verify(transactionManager).commit(txStatus);
        verify(transactionManager, never()).rollback(any());
    }

    @Test
    void should_setPropagation_supports_when_annotation_supports() throws Throwable {
        mockMethod("methodSupports");
        when(joinPoint.proceed()).thenReturn("OK");

        Object result = aspect.handleTransaction(joinPoint);

        assertEquals("OK", result);

        verify(transactionManager).getTransaction(defCaptor.capture());
        assertEquals(
                TransactionDefinition.PROPAGATION_SUPPORTS,
                defCaptor.getValue().getPropagationBehavior()
        );

        verify(transactionManager).commit(txStatus);
        verify(transactionManager, never()).rollback(any());
    }

    @Test
    void should_setPropagation_required_byDefault_when_otherwise() throws Throwable {
        mockMethod("methodRequired");
        when(joinPoint.proceed()).thenReturn("OK");

        Object result = aspect.handleTransaction(joinPoint);

        assertEquals("OK", result);

        verify(transactionManager).getTransaction(defCaptor.capture());
        assertEquals(
                TransactionDefinition.PROPAGATION_REQUIRED,
                defCaptor.getValue().getPropagationBehavior()
        );

        verify(transactionManager).commit(txStatus);
        verify(transactionManager, never()).rollback(any());
    }

    @Test
    void should_commit_when_proceed_successful() throws Throwable {
        mockMethod("methodRequired");
        when(joinPoint.proceed()).thenReturn(123);

        Object result = aspect.handleTransaction(joinPoint);

        assertEquals(123, result);
        verify(transactionManager).commit(txStatus);
        verify(transactionManager, never()).rollback(any());
    }

    // ---------------- Rollback / Commit behavior on Exceptions ----------------

    @Test
    void should_rollback_on_runtimeException_byDefault() throws Throwable {
        mockMethod("methodRequired");
        RuntimeException ex = new RuntimeException("boom");
        when(joinPoint.proceed()).thenThrow(ex);

        RuntimeException thrown = assertThrows(
                RuntimeException.class,
                () -> aspect.handleTransaction(joinPoint)
        );

        assertSame(ex, thrown);
        verify(transactionManager).rollback(txStatus);
        verify(transactionManager, never()).commit(txStatus);
    }

    @Test
    void should_commit_on_runtimeException_when_in_dontRollbackOn() throws Throwable {
        mockMethod("methodCustomExceptions");
        MyCustomRuntimeException ex = new MyCustomRuntimeException();
        when(joinPoint.proceed()).thenThrow(ex);

        assertThrows(
                MyCustomRuntimeException.class,
                () -> aspect.handleTransaction(joinPoint)
        );

        verify(transactionManager).commit(txStatus);
        verify(transactionManager, never()).rollback(any());
    }

    @Test
    void should_rollback_on_checkedException_when_in_rollbackOn() throws Throwable {
        mockMethod("methodCustomExceptions");
        MyCheckedException ex = new MyCheckedException();
        when(joinPoint.proceed()).thenThrow(ex);

        MyCheckedException thrown = assertThrows(
                MyCheckedException.class,
                () -> aspect.handleTransaction(joinPoint)
        );

        assertSame(ex, thrown);
        verify(transactionManager).rollback(txStatus);
        verify(transactionManager, never()).commit(txStatus);
    }

    @Test
    void should_commit_on_checkedException_when_notListed_anywhere() throws Throwable {
        mockMethod("methodRequired");
        MyCheckedException ex = new MyCheckedException();
        when(joinPoint.proceed()).thenThrow(ex);

        assertThrows(
                Exception.class,
                () -> aspect.handleTransaction(joinPoint)
        );

        verify(transactionManager).commit(txStatus);
        verify(transactionManager, never()).rollback(any());
    }

    // ---------------- Class-level annotation fallback test ----------------

    @Test
    void should_read_annotation_from_class_when_method_annotation_is_null() throws Throwable {
        @DomainTransactional(value = DomainTransactional.TxType.REQUIRES_NEW)
        class ClassAnnotatedTarget {
            public void unannotatedMethod() {}
        }

        ClassAnnotatedTarget target = new ClassAnnotatedTarget();
        Method unannotatedMethod = ClassAnnotatedTarget.class.getMethod("unannotatedMethod");

        when(methodSignature.getMethod()).thenReturn(unannotatedMethod);
        when(joinPoint.getTarget()).thenReturn(target);
        when(joinPoint.proceed()).thenReturn("OK");

        Object result = aspect.handleTransaction(joinPoint);

        assertEquals("OK", result);

        verify(transactionManager).getTransaction(defCaptor.capture());
        assertEquals(
                TransactionDefinition.PROPAGATION_REQUIRES_NEW,
                defCaptor.getValue().getPropagationBehavior()
        );

        verify(transactionManager).commit(txStatus);
        verify(transactionManager, never()).rollback(any());
    }
}