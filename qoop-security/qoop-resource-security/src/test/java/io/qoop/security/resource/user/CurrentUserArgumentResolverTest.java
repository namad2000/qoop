package io.qoop.security.resource.user;

import io.qoop.security.api.User;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.springframework.core.MethodParameter;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.core.OAuth2AuthenticatedPrincipal;
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

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class CurrentUserArgumentResolverTest {

    private CurrentUserArgumentResolver resolver;

    @BeforeEach
    void setUp() {
        resolver = new CurrentUserArgumentResolver();
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void supportsParameter_withCurrentUserAnnotation_returnsTrue() throws NoSuchMethodException {
        Method method = TestUserController.class.getMethod("getMyName", User.class);
        MethodParameter parameter = new MethodParameter(method, 0);

        assertTrue(resolver.supportsParameter(parameter));
    }

    @Test
    void supportsParameter_withoutCurrentUserAnnotation_returnsFalse() throws NoSuchMethodException {
        Method method = TestUserController.class.getMethod("unannotatedMethod", User.class);
        MethodParameter parameter = new MethodParameter(method, 0);

        assertFalse(resolver.supportsParameter(parameter));
    }

    @Test
    void supportsParameter_withWrongType_returnsFalse() throws NoSuchMethodException {
        Method method = TestUserController.class.getMethod("getString", String.class);
        MethodParameter parameter = new MethodParameter(method, 0);

        assertFalse(resolver.supportsParameter(parameter));
    }

    @Test
    void resolveArgument_noAuthentication_returnsSystemUser() throws Exception {
        SecurityContext context = mock(SecurityContext.class);
        when(context.getAuthentication()).thenReturn(null);

        try (MockedStatic<SecurityContextHolder> mockedHolder = mockStatic(SecurityContextHolder.class)) {
            mockedHolder.when(SecurityContextHolder::getContext).thenReturn(context);

            Object result = resolve();

            assertNotNull(result);
            assertInstanceOf(AuthenticatedUser.class, result);

            AuthenticatedUser user = (AuthenticatedUser) result;
            assertEquals(User.SYSTEM_USER, user.getName());
            assertFalse(user.isAuthenticated());
        }
    }

    @Test
    void resolveArgument_notAuthenticated_returnsSystemUser() throws Exception {
        Authentication authentication = mock(Authentication.class);
        when(authentication.isAuthenticated()).thenReturn(false);

        SecurityContext context = mock(SecurityContext.class);
        when(context.getAuthentication()).thenReturn(authentication);

        try (MockedStatic<SecurityContextHolder> mockedHolder = mockStatic(SecurityContextHolder.class)) {
            mockedHolder.when(SecurityContextHolder::getContext).thenReturn(context);

            Object result = resolve();

            AuthenticatedUser user = (AuthenticatedUser) result;
            assertEquals(User.SYSTEM_USER, user.getName());
            assertFalse(user.isAuthenticated());
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
                Map.of("alg", "none"),
                claims
        );

        List<SimpleGrantedAuthority> authorities = List.of(
                new SimpleGrantedAuthority("ROLE_ADMIN"),
                new SimpleGrantedAuthority("ROLE_USER")
        );

        JwtAuthenticationToken jwtAuthenticationToken = new JwtAuthenticationToken(jwt, authorities);
        SecurityContext context = mock(SecurityContext.class);
        when(context.getAuthentication()).thenReturn(jwtAuthenticationToken);

        try (MockedStatic<SecurityContextHolder> mockedHolder = mockStatic(SecurityContextHolder.class)) {
            mockedHolder.when(SecurityContextHolder::getContext).thenReturn(context);

            Object result = resolve();

            assertInstanceOf(AuthenticatedUser.class, result);
            AuthenticatedUser user = (AuthenticatedUser) result;

            assertEquals("jwtUser", user.getName());
            assertEquals("QoopTech", user.getOrganization());
            assertTrue(user.isVirtual());
            assertTrue(user.isAuthenticated());
            assertArrayEquals(new String[]{"ROLE_ADMIN", "ROLE_USER"}, user.getRoles());
        }
    }

    @Test
    void resolveArgument_opaqueTokenAuthentication_returnsFullAuthenticatedUser() throws Exception {
        OAuth2AuthenticatedPrincipal principal = mock(OAuth2AuthenticatedPrincipal.class);
        when(principal.getName()).thenReturn("opaqueUser");
        when(principal.getAttribute("organization")).thenReturn("QoopTech");
        when(principal.getAttribute("virtual")).thenReturn(true);

        List<SimpleGrantedAuthority> authorities = List.of(
                new SimpleGrantedAuthority("ROLE_ADMIN"),
                new SimpleGrantedAuthority("ROLE_USER")
        );

        Authentication authentication = mock(Authentication.class);
        when(authentication.isAuthenticated()).thenReturn(true);
        when(authentication.getPrincipal()).thenReturn(principal);
        doReturn(authorities).when(authentication).getAuthorities();

        SecurityContext context = mock(SecurityContext.class);
        when(context.getAuthentication()).thenReturn(authentication);

        try (MockedStatic<SecurityContextHolder> mockedHolder = mockStatic(SecurityContextHolder.class)) {
            mockedHolder.when(SecurityContextHolder::getContext).thenReturn(context);

            Object result = resolve();

            assertInstanceOf(AuthenticatedUser.class, result);
            AuthenticatedUser user = (AuthenticatedUser) result;

            assertEquals("opaqueUser", user.getName());
            assertEquals("QoopTech", user.getOrganization());
            assertTrue(user.isVirtual());
            assertTrue(user.isAuthenticated());
            assertArrayEquals(new String[]{"ROLE_ADMIN", "ROLE_USER"}, user.getRoles());
        }
    }

    @Test
    void resolveArgument_fallbackAuthentication_returnsAuthenticatedUser() throws Exception {
        Authentication authentication = new UsernamePasswordAuthenticationToken(
                "fallbackUser",
                null,
                List.of(new SimpleGrantedAuthority("ROLE_USER"))
        );

        SecurityContext context = mock(SecurityContext.class);
        when(context.getAuthentication()).thenReturn(authentication);

        try (MockedStatic<SecurityContextHolder> mockedHolder = mockStatic(SecurityContextHolder.class)) {
            mockedHolder.when(SecurityContextHolder::getContext).thenReturn(context);

            AuthenticatedUser user = (AuthenticatedUser) resolve();

            assertEquals("fallbackUser", user.getName());
            assertTrue(user.isAuthenticated());
            assertArrayEquals(new String[]{"ROLE_USER"}, user.getRoles());
        }
    }

    private Object resolve() throws Exception {
        Method method = TestUserController.class.getMethod("getMyName", User.class);
        MethodParameter parameter = new MethodParameter(method, 0);

        return resolver.resolveArgument(
                parameter,
                mock(ModelAndViewContainer.class),
                mock(NativeWebRequest.class),
                mock(WebDataBinderFactory.class)
        );
    }
}