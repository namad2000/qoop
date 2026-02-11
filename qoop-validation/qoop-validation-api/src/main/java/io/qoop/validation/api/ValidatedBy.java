package io.qoop.validation.api;

import io.qoop.validation.api.validator.AnnotationValidator;

import java.lang.annotation.*;

/**
 * @author Davood Akbari - 1404
 * daak1365@gmail.com
 * daak1365@yahoo.com
 * 09125188694
 */

@Target({ElementType.PARAMETER, ElementType.FIELD, ElementType.ANNOTATION_TYPE})
@Retention(RetentionPolicy.RUNTIME)
public @interface ValidatedBy {
    Class<? extends AnnotationValidator<?, ? extends Annotation>> value();
}
