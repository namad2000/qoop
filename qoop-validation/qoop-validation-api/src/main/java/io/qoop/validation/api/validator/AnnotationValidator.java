package io.qoop.validation.api.validator;

import java.lang.annotation.Annotation;

/**
 * @author Davood Akbari - 1404
 * daak1365@gmail.com
 * daak1365@yahoo.com
 * 09125188694
 */

public interface AnnotationValidator<T, A extends Annotation> {
    // Validate the value based on the annotation and field name
    void validate(T value, A annotation, String paramName);
}

