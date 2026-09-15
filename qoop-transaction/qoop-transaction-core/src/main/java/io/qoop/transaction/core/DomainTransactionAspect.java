package io.qoop.transaction.core;

import io.qoop.transaction.api.DomainTransactional;
import lombok.RequiredArgsConstructor;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.DefaultTransactionDefinition;

import java.lang.reflect.Method;

@Aspect
@Component
@Order(1) // Ensures high precedence execution among other aspects
@RequiredArgsConstructor
public class DomainTransactionAspect {

    private final PlatformTransactionManager transactionManager;

    /**
     * Intercepts methods or classes annotated with @DomainTransactional and manages the database transaction.
     */
    @Around("@within(io.qoop.transaction.api.DomainTransactional) || @annotation(io.qoop.transaction.api.DomainTransactional)")
    public Object handleTransaction(ProceedingJoinPoint joinPoint) throws Throwable {

        // Extract annotation manually from method or target class
        DomainTransactional domainTransactional = getAnnotation(joinPoint);

        if (domainTransactional == null) {
            return joinPoint.proceed();
        }

        DefaultTransactionDefinition def = getDefaultTransactionDefinition(domainTransactional);
        TransactionStatus status = transactionManager.getTransaction(def);

        try {
            Object result = joinPoint.proceed();
            transactionManager.commit(status);
            return result;
        } catch (Throwable ex) {
            if (shouldRollback(ex, domainTransactional)) {
                transactionManager.rollback(status);
            } else {
                transactionManager.commit(status);
            }
            throw ex;
        }
    }

    /**
     * Resolves the @DomainTransactional annotation giving precedence to the method level over class level.
     */
    protected DomainTransactional getAnnotation(ProceedingJoinPoint joinPoint) {
        if (joinPoint.getSignature() instanceof MethodSignature signature) {
            Method method = signature.getMethod();

            // 1. Check method-level annotation
            DomainTransactional annotation = method.getAnnotation(DomainTransactional.class);
            if (annotation != null) {
                return annotation;
            }
        }

        // 2. Fallback to class-level annotation
        if (joinPoint.getTarget() != null) {
            return joinPoint.getTarget().getClass().getAnnotation(DomainTransactional.class);
        }

        return null;
    }

    /**
     * Maps custom DomainTransactional configuration to Spring's DefaultTransactionDefinition.
     */
    private static DefaultTransactionDefinition getDefaultTransactionDefinition(DomainTransactional domainTransactional) {
        DefaultTransactionDefinition def = new DefaultTransactionDefinition();

        if (domainTransactional.value() == DomainTransactional.TxType.REQUIRES_NEW) {
            def.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRES_NEW);
        } else if (domainTransactional.value() == DomainTransactional.TxType.SUPPORTS) {
            def.setPropagationBehavior(TransactionDefinition.PROPAGATION_SUPPORTS);
        } else {
            def.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRED);
        }
        return def;
    }

    /**
     * Determines whether the current transaction should be rolled back based on exception rules.
     */
    private boolean shouldRollback(Throwable ex, DomainTransactional domainTransactional) {
        for (Class<? extends Throwable> dontEx : domainTransactional.dontRollbackOn()) {
            if (dontEx.isInstance(ex)) return false;
        }
        for (Class<? extends Throwable> rollEx : domainTransactional.rollbackOn()) {
            if (rollEx.isInstance(ex)) return true;
        }
        return ex instanceof RuntimeException;
    }
}