package io.qoop.security.resource.user;

import io.qoop.security.api.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtClaimNames;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;

import java.time.Instant;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class CurrentUserAspectTest {

    private CurrentUserAspect aspect;

    @BeforeEach
    void setUp() {
        aspect = new CurrentUserAspect();
    }

    @Test
    void resolveCurrentUser_noAuthentication_returnsSystemUser() {
        SecurityContext context = mock(SecurityContext.class);
        when(context.getAuthentication()).thenReturn(null);

        try (MockedStatic<SecurityContextHolder> mockedHolder = mockStatic(SecurityContextHolder.class)) {
            mockedHolder.when(SecurityContextHolder::getContext).thenReturn(context);

            Object result = aspect.resolveCurrentUser(); // فراخوانی متد عمومی (باید پکیج خصوصی یا عمود شود)

            assertNotNull(result);
            assertInstanceOf(AuthenticatedUser.class, result);
            assertEquals(User.SYSTEM_USER, ((AuthenticatedUser) result).getName());
        }
    }

    @Test
    void resolveCurrentUser_jwtAuthentication_returnsFullUser() {
        // 1. Mock JWT
        Map<String, Object> claims = Map.of(
                JwtClaimNames.SUB, "jwtUser",
                "organization", "QoopTech",
                "virtual", true
        );

        Jwt jwt = new Jwt("token", Instant.now(), Instant.now().plusSeconds(3600), Map.of("alg", "none"), claims);
        List<SimpleGrantedAuthority> authorities = List.of(new SimpleGrantedAuthority("ROLE_ADMIN"));
        JwtAuthenticationToken auth = new JwtAuthenticationToken(jwt, authorities);

        SecurityContext context = mock(SecurityContext.class);
        when(context.getAuthentication()).thenReturn(auth);

        try (MockedStatic<SecurityContextHolder> mockedHolder = mockStatic(SecurityContextHolder.class)) {
            mockedHolder.when(SecurityContextHolder::getContext).thenReturn(context);

            Object result = aspect.resolveCurrentUser();

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