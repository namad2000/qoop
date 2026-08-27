package io.qoop.validation.api;

import io.qoop.validation.api.validator.IPAddressValidator;

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
@ValidatedBy(IPAddressValidator.class)
public @interface IPAddress {
    Type type() default Type.ANY;

    enum Type {
        ANY,
        IPv4,
        IPv6
    }
}