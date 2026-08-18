package io.qoop.security.resource.user;

import io.qoop.security.api.CurrentUser;
import io.qoop.security.api.User;
import org.springframework.core.MethodParameter;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.core.OAuth2AuthenticatedPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.method.support.ModelAndViewContainer;

@Component
public class CurrentUserArgumentResolver implements HandlerMethodArgumentResolver {

    @Override
    public boolean supportsParameter(MethodParameter parameter) {
        // Check if the parameter has @CurrentUser annotation and is of type User
        return parameter.hasParameterAnnotation(CurrentUser.class) &&
                User.class.isAssignableFrom(parameter.getParameterType());
    }

    @Override
    public Object resolveArgument(MethodParameter parameter,
                                  ModelAndViewContainer mavContainer,
                                  NativeWebRequest webRequest,
                                  WebDataBinderFactory binderFactory) {

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        // Return system user if not authenticated
        if (authentication == null || !authentication.isAuthenticated()) {
            return AuthenticatedUser.builder()
                    .username(User.SYSTEM_USER)
                    .authenticated(false)
                    .build();
        }

        String[] roles = authentication
                .getAuthorities()
                .stream()
                .map(GrantedAuthority::getAuthority)
                .toArray(String[]::new);

        // 1. JWT mode (when the JWT profile is active)
        if (authentication instanceof JwtAuthenticationToken jwtToken) {
            Jwt jwt = jwtToken.getToken();

            return AuthenticatedUser.builder()
                    .username(jwt.getSubject())
                    .organization(jwt.getClaimAsString("organization"))
                    .roles(roles)
                    .virtual(Boolean.TRUE.equals(jwt.getClaimAsBoolean("virtual")))
                    .authenticated(true)
                    .build();
        }

        // 2. Introspection / Opaque Token mode (when the verify profile is active)
        if (authentication.getPrincipal() instanceof OAuth2AuthenticatedPrincipal principal) {

            return AuthenticatedUser.builder()
                    .username(principal.getName())
                    .organization(
                            principal.getAttribute("organization")
                    )
                    .roles(roles)
                    .virtual(Boolean.TRUE.equals(principal.getAttribute("virtual")))
                    .authenticated(true)
                    .build();
        }

        // Fallback mode
        return AuthenticatedUser.builder()
                .username(authentication.getName())
                .roles(roles)
                .authenticated(true)
                .build();
    }
}