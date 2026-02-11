package io.qoop.validation.core;

import io.qoop.validation.api.Constraint;
import io.qoop.validation.api.IsValid;
import io.qoop.validation.api.ValidatedBy;
import io.qoop.validation.api.validator.AnnotationValidator;
import lombok.SneakyThrows;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.Parameter;

public class Validator {

    // Validate the fields of an object
    @SneakyThrows
    @SuppressWarnings("unchecked")
    public static <T> void validate(T object) {
        if (object == null) return;

        Class<?> clazz = object.getClass();
        if (isPrimitiveOrWrapper(clazz) || clazz == String.class) return;

        for (Field field : clazz.getDeclaredFields()) {
            field.setAccessible(true);
            Object value = field.get(object);
            String fieldName = field.getName();

            for (Annotation ann : field.getAnnotations()) {
                // only proceed if annotation has your custom @Constraint
                if (ann.annotationType().isAnnotationPresent(Constraint.class)) {
                    ValidatedBy vb = ann.annotationType().getAnnotation(ValidatedBy.class);
                    if (vb != null) {
                        AnnotationValidator validator = vb.value().getDeclaredConstructor().newInstance();
                        validator.validate(value, ann, fieldName);
                    }
                }
            }

            // recursively validate nested objects (optional)
            if (field.isAnnotationPresent(IsValid.class)) {
                validate(value);
            }
        }
    }

    // Validate method parameters
    @SneakyThrows
    @SuppressWarnings("unchecked")
    public static <T> void validateMethodParams(Object[] args, Parameter[] params) {
        for (int i = 0; i < args.length; i++) {
            Object arg = args[i];
            Parameter param = params[i];

            for (Annotation ann : param.getAnnotations()) {
                // only proceed if annotation has your custom @Constraint
                if (ann.annotationType().isAnnotationPresent(Constraint.class)) {
                    ValidatedBy vb = ann.annotationType().getAnnotation(ValidatedBy.class);
                    if (vb != null) {
                        AnnotationValidator validator = vb.value().getDeclaredConstructor().newInstance();
                        String paramName = param.getName();
                        validator.validate(arg, ann, paramName);
                    }
                }
            }

            // recursively validate nested objects (optional)
            if (param.isAnnotationPresent(IsValid.class)) {
                validate(arg);
            }
        }
    }

    // Helper method
    public static boolean isPrimitiveOrWrapper(Class<?> clazz) {
        return clazz.isPrimitive()
                || clazz == Byte.class
                || clazz == Short.class
                || clazz == Integer.class
                || clazz == Long.class
                || clazz == Float.class
                || clazz == Double.class
                || clazz == Boolean.class
                || clazz == Character.class;
    }
}
