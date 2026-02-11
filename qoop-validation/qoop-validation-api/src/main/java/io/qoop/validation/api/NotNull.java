package io.qoop.validation.api;

import io.qoop.validation.api.validator.NotNullValidator;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Constraint
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.FIELD, ElementType.PARAMETER})
@ValidatedBy(NotNullValidator.class)
public @interface NotNull {
}
