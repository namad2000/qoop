package io.qoop.validation.core;

import io.qoop.validation.api.Constraint;
import io.qoop.validation.api.IsValid;
import io.qoop.validation.api.ValidatedBy;
import io.qoop.validation.api.validator.AnnotationValidator;
import lombok.SneakyThrows;
import org.springframework.beans.factory.config.AutowireCapableBeanFactory;
import org.springframework.stereotype.Component;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.Parameter;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Core validation engine responsible for validating objects, fields, and method parameters.
 * Dynamically instantiates and autowires validators via Spring's AutowireCapableBeanFactory.
 *
 * @author Davood Akbari - 1404
 * daak1365@gmail.com
 * daak1365@yahoo.com
 * 09125188694
 */
@Component
public class Validator {

    private final AutowireCapableBeanFactory beanFactory;

    // Cache for instantiated validators to avoid redundant reflections and autowiring overhead
    private final Map<Class<?>, AnnotationValidator<?, ?>> validatorCache = new ConcurrentHashMap<>();

    public Validator(AutowireCapableBeanFactory beanFactory) {
        this.beanFactory = beanFactory;
    }

    /**
     * Resolves and returns a validator instance managed and autowired by Spring, caching it for subsequent calls.
     */
    @SuppressWarnings({"unchecked", "rawtypes"})
    private AnnotationValidator getOrCreateValidator(Class<? extends AnnotationValidator> clazz) {
        return validatorCache.computeIfAbsent(clazz, key ->
            (AnnotationValidator) beanFactory.createBean(key)
        );
    }

    /**
     * Recursively validates annotated fields of the given target object.
     *
     * @param object the instance to validate
     */
    @SneakyThrows
    @SuppressWarnings("unchecked")
    public <T> void validate(T object) {
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
                        AnnotationValidator validator = getOrCreateValidator(vb.value());
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

    /**
     * Validates method execution arguments against parameter constraint annotations.
     *
     * @param args   method runtime argument values
     * @param params reflection parameter metadata array
     */
    @SneakyThrows
    @SuppressWarnings("unchecked")
    public <T> void validateMethodParams(Object[] args, Parameter[] params) {
        if (args == null || params == null) return;

        for (int i = 0; i < args.length; i++) {
            Object arg = args[i];
            Parameter param = params[i];

            for (Annotation ann : param.getAnnotations()) {
                // only proceed if annotation has your custom @Constraint
                if (ann.annotationType().isAnnotationPresent(Constraint.class)) {
                    ValidatedBy vb = ann.annotationType().getAnnotation(ValidatedBy.class);
                    if (vb != null) {
                        AnnotationValidator validator = getOrCreateValidator(vb.value());
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

    /**
     * Checks whether the target class is a primitive or standard wrapper type.
     */
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