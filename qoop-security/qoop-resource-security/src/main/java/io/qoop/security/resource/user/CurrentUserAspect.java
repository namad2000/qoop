package io.qoop.security.resource.user;

import io.qoop.security.api.CurrentUser;
import io.qoop.security.api.User;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.stereotype.Component;

import java.lang.reflect.Method;
import java.lang.reflect.Parameter;

@Aspect
@Component
public class CurrentUserAspect {

    @Around("@annotation(io.qoop.security.api.CurrentUser) || execution(* *(.., @io.qoop.security.api.CurrentUser (*), ..))")
    public Object injectCurrentUser(ProceedingJoinPoint joinPoint) throws Throwable {

        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        Method method = signature.getMethod();
        Parameter[] parameters = method.getParameters();
        Object[] args = joinPoint.getArgs();

        for (int i = 0; i < parameters.length; i++) {
            if (parameters[i].isAnnotationPresent(CurrentUser.class)) {
                if (args[i] == null) {
                    args[i] = resolveCurrentUser();
                }
            }
        }

        return joinPoint.proceed(args);
    }

    public Object resolveCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null || !authentication.isAuthenticated()) {
            return AuthenticatedUser.builder()
                    .username(User.SYSTEM_USER)
                    .build();
        }

        if (authentication instanceof JwtAuthenticationToken jwtToken) {
            Jwt jwt = jwtToken.getToken();

            String[] roles = authentication.getAuthorities().stream()
                    .map(Object::toString)
                    .toArray(String[]::new);

            return AuthenticatedUser.builder()
                    .username(jwt.getSubject())
                    .organization(jwt.getClaimAsString("organization"))
                    .roles(roles)
                    .virtual(Boolean.TRUE.equals(jwt.getClaimAsBoolean("virtual")))
                    .authenticated(true)
                    .build();
        }

        return AuthenticatedUser.builder()
                .username(authentication.getName())
                .build();
    }
}