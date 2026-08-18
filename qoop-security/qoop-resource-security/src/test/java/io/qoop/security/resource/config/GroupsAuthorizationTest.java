// GroupsAuthorizationTest.java

package io.qoop.security.resource.config;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertTrue;

class GroupsAuthorizationTest {

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void authentication_shouldContainAllUsersRole() {

        UsernamePasswordAuthenticationToken authentication =
                new UsernamePasswordAuthenticationToken(
                        "user",
                        null,
                        List.of(new SimpleGrantedAuthority("ROLE_ALL_USERS"))
                );

        SecurityContextHolder
                .getContext()
                .setAuthentication(authentication);

        assertTrue(
                authentication.getAuthorities()
                        .stream()
                        .anyMatch(authority -> "ROLE_ALL_USERS".equals(authority.getAuthority()))
        );
    }

    @Test
    void authentication_shouldContainAdminRole() {

        UsernamePasswordAuthenticationToken authentication =
                new UsernamePasswordAuthenticationToken(
                        "admin",
                        null,
                        List.of(new SimpleGrantedAuthority("ROLE_ADMIN"))
                );

        SecurityContextHolder
                .getContext()
                .setAuthentication(authentication);

        assertTrue(
                authentication.getAuthorities()
                        .stream()
                        .anyMatch(authority -> "ROLE_ADMIN".equals(authority.getAuthority()))
        );
    }
}