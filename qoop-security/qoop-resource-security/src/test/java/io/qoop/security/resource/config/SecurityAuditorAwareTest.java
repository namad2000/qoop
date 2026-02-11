package io.qoop.security.resource.config;

import io.qoop.security.resource.SecurityAuditorAware;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class SecurityAuditorAwareTest {

    private SecurityAuditorAware auditorAware;

    @BeforeEach
    void setUp() {
        auditorAware = new SecurityAuditorAware();
    }

    @AfterEach
    void tearDown() {
        // Clear the SecurityContext after each test
        SecurityContextHolder.clearContext();
    }

    @Test
    void testGetCurrentAuditor_whenNoAuthentication() {
        // No Authentication present in SecurityContext
        SecurityContextHolder.clearContext();

        Optional<String> auditor = auditorAware.getCurrentAuditor();

        assertTrue(auditor.isPresent());
        assertEquals("system", auditor.get());
    }

    @Test
    void testGetCurrentAuditor_whenAuthenticationNotAuthenticated() {
        // Mock Authentication that is not authenticated
        Authentication authentication = mock(Authentication.class);
        when(authentication.isAuthenticated()).thenReturn(false);

        // Place the mocked Authentication in a mocked SecurityContext
        SecurityContext securityContext = mock(SecurityContext.class);
        when(securityContext.getAuthentication()).thenReturn(authentication);

        SecurityContextHolder.setContext(securityContext);

        Optional<String> auditor = auditorAware.getCurrentAuditor();

        // Expect "system" to be returned
        assertTrue(auditor.isPresent());
        assertEquals("system", auditor.get());
    }

    @Test
    void testGetCurrentAuditor_whenAuthenticationAuthenticated() {
        // Mock Authentication that is authenticated
        Authentication authentication = mock(Authentication.class);
        when(authentication.isAuthenticated()).thenReturn(true);
        when(authentication.getName()).thenReturn("john.doe");

        // Place the mocked Authentication in a mocked SecurityContext
        SecurityContext securityContext = mock(SecurityContext.class);
        when(securityContext.getAuthentication()).thenReturn(authentication);

        SecurityContextHolder.setContext(securityContext);

        Optional<String> auditor = auditorAware.getCurrentAuditor();

        // Expect the username to be returned
        assertTrue(auditor.isPresent());
        assertEquals("john.doe", auditor.get());
    }

    @Test
    void testGetCurrentAuditor_whenAuthenticationNameIsNull() {
        // Mock Authentication with null username
        Authentication authentication = mock(Authentication.class);
        when(authentication.isAuthenticated()).thenReturn(true);
        when(authentication.getName()).thenReturn(null);

        SecurityContext securityContext = mock(SecurityContext.class);
        when(securityContext.getAuthentication()).thenReturn(authentication);

        SecurityContextHolder.setContext(securityContext);

        Optional<String> auditor = auditorAware.getCurrentAuditor();

        // Expect Optional to be present but username to be null
        assertFalse(auditor.isPresent());
    }
}
