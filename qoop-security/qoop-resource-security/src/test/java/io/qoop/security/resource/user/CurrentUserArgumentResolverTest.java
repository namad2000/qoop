package io.qoop.security.resource.user;

import io.qoop.security.api.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.springframework.core.MethodParameter;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.ModelAndViewContainer;

import java.lang.reflect.Method;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Author: davood akbari
 * Email: daak1365@gmail.com
 * Created: 1/4/2026 4:38 PM
 * Package: ir.online.commons.security.resource.user
 */


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
        // Mock authentication
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
            assertTrue(result instanceof AuthenticatedUser);
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
            assertTrue(result instanceof AuthenticatedUser);
            assertEquals(User.SYSTEM_USER, ((AuthenticatedUser) result).getName());
        }
    }
}
