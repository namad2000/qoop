package io.qoop.transaction.api;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target({ElementType.METHOD, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
public @interface DomainTransaction {

    TxType value() default TxType.REQUIRED;

    Class<? extends Throwable>[] rollbackOn() default {RuntimeException.class};

    Class<? extends Throwable>[] dontRollbackOn() default {};

    enum TxType {
        REQUIRED, REQUIRES_NEW, SUPPORTS
    }
}
