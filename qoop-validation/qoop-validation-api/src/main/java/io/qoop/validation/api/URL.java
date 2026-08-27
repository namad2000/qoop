package io.qoop.validation.api;

import io.qoop.validation.api.validator.URLValidator;

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
@ValidatedBy(URLValidator.class)
public @interface URL {
    String protocol() default "";

    String host() default "";

    int port() default -1;
}