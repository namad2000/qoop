package io.qoop.validation.core;

import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.stereotype.Component;

import java.lang.reflect.Method;
import java.lang.reflect.Parameter;

/**
 * Aspect responsible for intercepting method executions in classes annotated with @UseCaseService.
 * Triggers parameter and field validation via the Spring-managed {@link Validator} bean.
 *
 * @author Davood Akbari - 1404
 * daak1365@gmail.com
 * daak1365@yahoo.com
 * 09125188694
 */

@Aspect
@Component
public class ValidationAspect {

    private final Validator validator;

    public ValidationAspect(Validator validator) {
        this.validator = validator;
    }

    /**
     * Intercepts execution of use-case service methods to validate method arguments and inner fields.
     *
     * @param joinPoint execution join point metadata
     */
    @Before("within(@io.qoop.filter.bean.api.UseCaseService *)")
    public void validateMethod(JoinPoint joinPoint) {
        Method method = ((MethodSignature) joinPoint.getSignature()).getMethod();
        Object[] args = joinPoint.getArgs();
        Parameter[] params = method.getParameters();

        // 1. Validate method execution parameters
        validator.validateMethodParams(args, params);

        // 2. Recursively validate object graph for each parameter argument
        if (args != null) {
            for (Object arg : args) {
                if (arg != null) {
                    validator.validate(arg);
                }
            }
        }
    }
}