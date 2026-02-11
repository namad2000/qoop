package io.qoop.transaction.core;


import io.qoop.transaction.api.DomainTransaction;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.DefaultTransactionDefinition;

@Aspect
@Component
public class DomainTransactionAspect {

    private final PlatformTransactionManager transactionManager;

    public DomainTransactionAspect(PlatformTransactionManager transactionManager) {
        this.transactionManager = transactionManager;
    }

    @Around("@within(domainTransaction) || @annotation(domainTransaction)")
    public Object handleTransaction(ProceedingJoinPoint joinPoint, DomainTransaction domainTransaction) throws Throwable {
        // If the method does not have the annotation, get it from the class
        if (domainTransaction == null) {
            domainTransaction = joinPoint.getTarget().getClass().getAnnotation(DomainTransaction.class);
        }

        DefaultTransactionDefinition def = getDefaultTransactionDefinition(domainTransaction);

        TransactionStatus status = transactionManager.getTransaction(def);
        try {
            Object result = joinPoint.proceed();
            transactionManager.commit(status);
            return result;
        } catch (Throwable ex) {
            if (shouldRollback(ex, domainTransaction)) {
                transactionManager.rollback(status);
            } else {
                transactionManager.commit(status);
            }
            throw ex;
        }
    }

    private static DefaultTransactionDefinition getDefaultTransactionDefinition(DomainTransaction domainTransaction) {
        DefaultTransactionDefinition def = new DefaultTransactionDefinition();

        // 1. Set propagation behavior based on our custom enum
        if (domainTransaction.value() == DomainTransaction.TxType.REQUIRES_NEW) {
            def.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRES_NEW);
        } else if (domainTransaction.value() == DomainTransaction.TxType.SUPPORTS) {
            def.setPropagationBehavior(TransactionDefinition.PROPAGATION_SUPPORTS);
        } else {
            def.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRED);
        }
        return def;
    }

    private boolean shouldRollback(Throwable ex, DomainTransaction domainTransaction) {
        // Check dontRollbackOn list
        for (Class<? extends Throwable> dontEx : domainTransaction.dontRollbackOn()) {
            if (dontEx.isInstance(ex)) return false;
        }
        // Check rollbackOn list
        for (Class<? extends Throwable> rollEx : domainTransaction.rollbackOn()) {
            if (rollEx.isInstance(ex)) return true;
        }
        return ex instanceof RuntimeException; // default behavior
    }
}
