// GroupsOpaqueTokenIntrospectorTest.java

package io.qoop.security.resource.config;

import org.junit.jupiter.api.Test;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.core.DefaultOAuth2AuthenticatedPrincipal;
import org.springframework.security.oauth2.core.OAuth2AuthenticatedPrincipal;
import org.springframework.security.oauth2.server.resource.introspection.OpaqueTokenIntrospector;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class GroupsOpaqueTokenIntrospectorTest {

    @Test
    void shouldConvertListGroupsToAuthorities() {

        OAuth2AuthenticatedPrincipal principal =
                new DefaultOAuth2AuthenticatedPrincipal(
                        "user",
                        Map.of(
                                "organization",
                                "QoopTech",
                                "groups",
                                Arrays.asList(
                                        "ADMIN",
                                        "ALL_USERS"
                                )
                        ),
                        Collections.singletonList(
                                new SimpleGrantedAuthority(
                                        "ROLE_USER"
                                )
                        )
                );

        OpaqueTokenIntrospector delegate = mock(OpaqueTokenIntrospector.class);
        when(delegate.introspect("token")).thenReturn(principal);

        GroupsOpaqueTokenIntrospector introspector = new GroupsOpaqueTokenIntrospector(
                delegate
        );

        OAuth2AuthenticatedPrincipal result = introspector.introspect("token");

        assertEquals("user", result.getName());
        assertTrue(hasAuthority(result.getAuthorities(), "ROLE_USER"));
        assertTrue(hasAuthority(result.getAuthorities(), "ROLE_ADMIN"));
        assertTrue(hasAuthority(result.getAuthorities(), "ROLE_ALL_USERS"));
    }

    @Test
    void shouldConvertCommaSeparatedGroupsToAuthorities() {

        OAuth2AuthenticatedPrincipal principal = new DefaultOAuth2AuthenticatedPrincipal(
                "user",
                Map.of(
                        "groups",
                        "ADMIN,USER"
                ),
                Collections.emptyList()
        );

        OpaqueTokenIntrospector delegate = mock(OpaqueTokenIntrospector.class);

        when(delegate.introspect("token")).thenReturn(principal);

        GroupsOpaqueTokenIntrospector introspector = new GroupsOpaqueTokenIntrospector(delegate);

        OAuth2AuthenticatedPrincipal result = introspector.introspect("token");

        assertTrue(hasAuthority(result.getAuthorities(), "ROLE_ADMIN"));
        assertTrue(hasAuthority(result.getAuthorities(), "ROLE_USER"));
    }

    @Test
    void shouldKeepExistingRolePrefix() {

        OAuth2AuthenticatedPrincipal principal =
                new DefaultOAuth2AuthenticatedPrincipal(
                        "user",
                        Map.of(
                                "groups",
                                Arrays.asList(
                                        "ROLE_ADMIN"
                                )
                        ),
                        Collections.emptyList()
                );

        OpaqueTokenIntrospector delegate = mock(OpaqueTokenIntrospector.class);

        when(delegate.introspect("token")).thenReturn(principal);

        GroupsOpaqueTokenIntrospector introspector = new GroupsOpaqueTokenIntrospector(delegate);

        OAuth2AuthenticatedPrincipal result =
                introspector.introspect("token");

        assertTrue(hasAuthority(result.getAuthorities(), "ROLE_ADMIN"));

        assertFalse(hasAuthority(result.getAuthorities(), "ROLE_ROLE_ADMIN"));
    }

    @Test
    void shouldPreserveExistingAuthorities() {

        OAuth2AuthenticatedPrincipal principal =
                new DefaultOAuth2AuthenticatedPrincipal(
                        "user",
                        Map.of(
                                "groups",
                                Arrays.asList("ADMIN")
                        ),
                        Arrays.asList(
                                new SimpleGrantedAuthority(
                                        "SCOPE_read"
                                ),
                                new SimpleGrantedAuthority(
                                        "ROLE_USER"
                                )
                        )
                );

        OpaqueTokenIntrospector delegate = mock(OpaqueTokenIntrospector.class);

        when(delegate.introspect("token")).thenReturn(principal);

        GroupsOpaqueTokenIntrospector introspector = new GroupsOpaqueTokenIntrospector(delegate);

        OAuth2AuthenticatedPrincipal result = introspector.introspect("token");

        assertTrue(hasAuthority(result.getAuthorities(), "SCOPE_read"));
        assertTrue(hasAuthority(result.getAuthorities(), "ROLE_USER"));
        assertTrue(hasAuthority(result.getAuthorities(), "ROLE_ADMIN"));
    }

    @Test
    void shouldHandleNullGroups() {

        OAuth2AuthenticatedPrincipal principal =
                new DefaultOAuth2AuthenticatedPrincipal(
                        "user",
                        Collections.singletonMap(
                                "organization",
                                "QoopTech"
                        ),
                        Collections.emptyList()
                );

        OpaqueTokenIntrospector delegate = mock(OpaqueTokenIntrospector.class);

        when(delegate.introspect("token")).thenReturn(principal);

        GroupsOpaqueTokenIntrospector introspector = new GroupsOpaqueTokenIntrospector(delegate);

        OAuth2AuthenticatedPrincipal result = introspector.introspect("token");

        assertNotNull(result);
        assertTrue(result.getAuthorities().isEmpty());
    }

    private boolean hasAuthority(Collection<? extends GrantedAuthority> authorities, String authority) {
        return authorities.stream().anyMatch(item -> authority.equals(item.getAuthority()));
    }
}