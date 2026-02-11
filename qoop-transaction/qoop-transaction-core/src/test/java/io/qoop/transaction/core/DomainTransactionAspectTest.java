package io.qoop.transaction.core;

import io.qoop.transaction.api.DomainTransaction;
import org.aspectj.lang.ProceedingJoinPoint;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.DefaultTransactionDefinition;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DomainTransactionAspectTest {

    @Mock
    PlatformTransactionManager transactionManager;

    @Mock
    ProceedingJoinPoint joinPoint;

    @Mock
    TransactionStatus txStatus;

    @Captor
    ArgumentCaptor<DefaultTransactionDefinition> defCaptor;

    private DomainTransactionAspect aspect;

    @BeforeEach
    void setUp() {
        // Create the Aspect with a mocked TransactionManager
        aspect = new DomainTransactionAspect(transactionManager);

        // Whenever getTransaction is called, return a mocked TransactionStatus
        when(transactionManager.getTransaction(any(TransactionDefinition.class)))
                .thenReturn(txStatus);
    }

    // ---------------- Propagation-related tests (no Exception) ----------------

    @Test
    void should_setPropagation_requiresNew_when_annotation_requiresNew() throws Throwable {
        // Mock the annotation with REQUIRES_NEW value
        DomainTransaction ann = mock(DomainTransaction.class);
        when(ann.value()).thenReturn(DomainTransaction.TxType.REQUIRES_NEW);

        // Successful execution of the target method
        when(joinPoint.proceed()).thenReturn("OK");

        Object result = aspect.handleTransaction(joinPoint, ann);

        // Verify the result
        assertEquals("OK", result);

        // Verify that propagation behavior is set correctly
        verify(transactionManager).getTransaction(defCaptor.capture());
        assertEquals(
                TransactionDefinition.PROPAGATION_REQUIRES_NEW,
                defCaptor.getValue().getPropagationBehavior()
        );

        // Transaction should be committed and rollback must not be called
        verify(transactionManager).commit(txStatus);
        verify(transactionManager, never()).rollback(any());
    }

    @Test
    void should_setPropagation_supports_when_annotation_supports() throws Throwable {
        DomainTransaction ann = mock(DomainTransaction.class);
        when(ann.value()).thenReturn(DomainTransaction.TxType.SUPPORTS);

        when(joinPoint.proceed()).thenReturn("OK");

        Object result = aspect.handleTransaction(joinPoint, ann);

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
        // Default case (neither REQUIRES_NEW nor SUPPORTS)
        DomainTransaction ann = mock(DomainTransaction.class);
        when(ann.value()).thenReturn(DomainTransaction.TxType.REQUIRED);

        when(joinPoint.proceed()).thenReturn("OK");

        Object result = aspect.handleTransaction(joinPoint, ann);

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
        // If the target method executes successfully, transaction must be committed
        DomainTransaction ann = mock(DomainTransaction.class);
        when(ann.value()).thenReturn(DomainTransaction.TxType.REQUIRED);

        when(joinPoint.proceed()).thenReturn(123);

        Object result = aspect.handleTransaction(joinPoint, ann);

        assertEquals(123, result);
        verify(transactionManager).commit(txStatus);
        verify(transactionManager, never()).rollback(any());
    }

    // ---------------- Rollback / Commit behavior on Exceptions ----------------

    @Test
    void should_rollback_on_runtimeException_byDefault() throws Throwable {
        // By default, RuntimeException should trigger a rollback
        DomainTransaction ann = mock(DomainTransaction.class);
        when(ann.value()).thenReturn(DomainTransaction.TxType.REQUIRED);
        when(ann.dontRollbackOn()).thenReturn(new Class[0]);
        when(ann.rollbackOn()).thenReturn(new Class[0]);

        RuntimeException ex = new RuntimeException("boom");
        when(joinPoint.proceed()).thenThrow(ex);

        RuntimeException thrown =
                assertThrows(RuntimeException.class,
                        () -> aspect.handleTransaction(joinPoint, ann));

        assertSame(ex, thrown);

        verify(transactionManager).rollback(txStatus);
        verify(transactionManager, never()).commit(txStatus);
    }

    @Test
    void should_commit_on_runtimeException_when_in_dontRollbackOn() throws Throwable {
        // If RuntimeException is listed in dontRollbackOn, rollback must not happen
        DomainTransaction ann = mock(DomainTransaction.class);
        when(ann.value()).thenReturn(DomainTransaction.TxType.REQUIRED);
        when(ann.dontRollbackOn()).thenReturn(new Class[]{RuntimeException.class});

        RuntimeException ex = new RuntimeException("boom");
        when(joinPoint.proceed()).thenThrow(ex);

        assertThrows(RuntimeException.class,
                () -> aspect.handleTransaction(joinPoint, ann));

        verify(transactionManager).commit(txStatus);
        verify(transactionManager, never()).rollback(any());
    }

    @Test
    void should_rollback_on_checkedException_when_in_rollbackOn() throws Throwable {
        // Checked exception listed in rollbackOn should cause rollback
        class MyCheckedException extends Exception {
            MyCheckedException(String m) {
                super(m);
            }
        }

        DomainTransaction ann = mock(DomainTransaction.class);
        when(ann.value()).thenReturn(DomainTransaction.TxType.REQUIRED);
        when(ann.dontRollbackOn()).thenReturn(new Class[0]);
        when(ann.rollbackOn()).thenReturn(new Class[]{MyCheckedException.class});

        MyCheckedException ex = new MyCheckedException("checked");
        when(joinPoint.proceed()).thenThrow(ex);

        Exception thrown =
                assertThrows(Exception.class,
                        () -> aspect.handleTransaction(joinPoint, ann));

        assertSame(ex, thrown);

        verify(transactionManager).rollback(txStatus);
        verify(transactionManager, never()).commit(txStatus);
    }

    @Test
    void should_commit_on_checkedException_when_notListed_anywhere() throws Throwable {
        // Checked exception that is not RuntimeException and not listed in rollbackOn → commit
        class MyCheckedException extends Exception {
            MyCheckedException(String m) {
                super(m);
            }
        }

        DomainTransaction ann = mock(DomainTransaction.class);
        when(ann.value()).thenReturn(DomainTransaction.TxType.REQUIRED);
        when(ann.dontRollbackOn()).thenReturn(new Class[0]);
        when(ann.rollbackOn()).thenReturn(new Class[0]);

        MyCheckedException ex = new MyCheckedException("checked");
        when(joinPoint.proceed()).thenThrow(ex);

        assertThrows(Exception.class,
                () -> aspect.handleTransaction(joinPoint, ann));

        verify(transactionManager).commit(txStatus);
        verify(transactionManager, never()).rollback(any());
    }

    // ---------------- Class-level annotation fallback test ----------------

    @Test
    void should_read_annotation_from_class_when_method_annotation_is_null() throws Throwable {
        // If the method does not have the annotation, it should be read from the class
        @DomainTransaction(value = DomainTransaction.TxType.REQUIRES_NEW)
        class Target {
        }

        Target target = new Target();
        when(joinPoint.getTarget()).thenReturn(target);
        when(joinPoint.proceed()).thenReturn("OK");

        Object result = aspect.handleTransaction(joinPoint, null);

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
