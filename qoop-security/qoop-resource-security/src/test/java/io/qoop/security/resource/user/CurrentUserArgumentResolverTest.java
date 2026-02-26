package io.qoop.security.resource.user;

import io.qoop.security.api.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.springframework.core.MethodParameter;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtClaimNames;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.ModelAndViewContainer;

import java.lang.reflect.Method;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class CurrentUserArgumentResolverTest {

    private CurrentUserArgumentResolver resolver;

    @BeforeEach
    void setUp() {
        resolver = new CurrentUserArgumentResolver();
    }

    @Test
    void supportsParameter_withCurrentUserAnnotation_returnsTrue() throws NoSuchMethodException {
        Method method = TestUserController.class.getMethod("getMyName", User.class);
        MethodParameter parameter = new MethodParameter(method, 0);
        assertTrue(resolver.supportsParameter(parameter));
    }

    @Test
    void resolveArgument_authenticatedUser_returnsAuthenticatedUser() throws Exception {
        Authentication authentication = mock(Authentication.class);
        when(authentication.isAuthenticated()).thenReturn(true);
        when(authentication.getName()).thenReturn("JohnDoe");
        SecurityContext context = mock(SecurityContext.class);
        when(context.getAuthentication()).thenReturn(authentication);

        try (MockedStatic<SecurityContextHolder> mockedHolder = mockStatic(SecurityContextHolder.class)) {
            mockedHolder.when(SecurityContextHolder::getContext).thenReturn(context);
            Method method = TestUserController.class.getMethod("getMyName", User.class);
            MethodParameter parameter = new MethodParameter(method, 0);
            Object result = resolver.resolveArgument(parameter, mock(ModelAndViewContainer.class),
                    mock(NativeWebRequest.class), mock(WebDataBinderFactory.class));

            assertNotNull(result);
            assertInstanceOf(AuthenticatedUser.class, result);
            assertEquals("JohnDoe", ((AuthenticatedUser) result).getName());
        }
    }

    @Test
    void resolveArgument_noAuthentication_returnsSystemUser() throws Exception {
        SecurityContext context = mock(SecurityContext.class);
        when(context.getAuthentication()).thenReturn(null);

        try (MockedStatic<SecurityContextHolder> mockedHolder = mockStatic(SecurityContextHolder.class)) {
            mockedHolder.when(SecurityContextHolder::getContext).thenReturn(context);
            Method method = TestUserController.class.getMethod("getMyName", User.class);
            MethodParameter parameter = new MethodParameter(method, 0);
            Object result = resolver.resolveArgument(parameter, mock(ModelAndViewContainer.class),
                    mock(NativeWebRequest.class), mock(WebDataBinderFactory.class));

            assertNotNull(result);
            assertInstanceOf(AuthenticatedUser.class, result);
            assertEquals(User.SYSTEM_USER, ((AuthenticatedUser) result).getName());
        }
    }

    @Test
    void resolveArgument_jwtAuthentication_returnsFullAuthenticatedUser() throws Exception {
        Map<String, Object> claims = Map.of(
                JwtClaimNames.SUB, "jwtUser",
                "organization", "QoopTech",
                "virtual", true
        );

        Jwt jwt = new Jwt(
                "token-value",
                Instant.now(),
                Instant.now().plusSeconds(3600),
                Map.of("header", "value"),
                claims
        );

        List<SimpleGrantedAuthority> authorities = List.of(
                new SimpleGrantedAuthority("ROLE_ADMIN")
        );

        JwtAuthenticationToken jwtAuthToken = new JwtAuthenticationToken(jwt, authorities);

        SecurityContext context = mock(SecurityContext.class);
        when(context.getAuthentication()).thenReturn(jwtAuthToken);

        try (MockedStatic<SecurityContextHolder> mockedHolder = mockStatic(SecurityContextHolder.class)) {
            mockedHolder.when(SecurityContextHolder::getContext).thenReturn(context);

            Method method = TestUserController.class.getMethod("getMyName", User.class);
            MethodParameter parameter = new MethodParameter(method, 0);

            Object result = resolver.resolveArgument(
                    parameter,
                    mock(ModelAndViewContainer.class),
                    mock(NativeWebRequest.class),
                    mock(WebDataBinderFactory.class)
            );

            assertNotNull(result);
            assertInstanceOf(AuthenticatedUser.class, result);
            AuthenticatedUser user = (AuthenticatedUser) result;

            assertEquals("jwtUser", user.getName());
            assertEquals("QoopTech", user.getOrganization());
            assertTrue(user.isVirtual());
            assertTrue(user.isAuthenticated());
        }
    }
}