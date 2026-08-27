package io.qoop.validation.api;

import io.qoop.validation.api.validator.RangeValidator;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * @author Davood Akbari - 1404
 * daak1365@gmail.com
 * daak1365@yahoo.com
 * 09125188694
 */
@Constraint
@Target({ElementType.FIELD, ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
@ValidatedBy(RangeValidator.class)
public @interface Range {
    long min() default 0L;

    long max() default Long.MAX_VALUE;
}