package io.qoop.validation.core;

import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.stereotype.Component;

import java.lang.reflect.Method;
import java.lang.reflect.Parameter;

/**
 * @author Davood Akbari - 1404
 * daak1365@gmail.com
 * daak1365@yahoo.com
 * 09125188694
 */

@Aspect
@Component
public class ValidationAspect {

    // Before advice that runs before method execution in classes annotated with @UseCaseService
    @Before("within(@io.qoop.filter.bean.api.UseCaseService *)")
    public void validateMethod(JoinPoint joinPoint) {
        Method method = ((MethodSignature) joinPoint.getSignature()).getMethod();

        // Validate method parameters
        Object[] args = joinPoint.getArgs();
        Parameter[] params = method.getParameters();
        Validator.validateMethodParams(args, params);

        // Validate any field in the method parameters recursively
        for (Object arg : args) {
            if (arg != null) {
                Validator.validate(arg);
            }
        }
    }

    // Aspect for validating fields recursively (kept for backward compatibility)
    @Before("within(@io.qoop.filter.bean.api.UseCaseService *)")
    public void validateFields(JoinPoint joinPoint) {
        Object[] args = joinPoint.getArgs();

        for (Object arg : args) {
            if (arg != null) {
                Validator.validate(arg);
            }
        }
    }
}


