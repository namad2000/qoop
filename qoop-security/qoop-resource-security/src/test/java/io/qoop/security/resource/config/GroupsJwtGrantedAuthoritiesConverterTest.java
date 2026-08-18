// GroupsJwtGrantedAuthoritiesConverterTest.java

package io.qoop.security.resource.config;

import org.junit.jupiter.api.Test;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;

import java.time.Instant;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

class GroupsJwtGrantedAuthoritiesConverterTest {

    private final GroupsJwtGrantedAuthoritiesConverter converter =
            new GroupsJwtGrantedAuthoritiesConverter();

    @Test
    void shouldConvertGroupsToRoleAuthorities() {

        Jwt jwt = jwt(
                Arrays.asList(
                        "ADMIN",
                        "ALL_USERS"
                )
        );

        Collection<GrantedAuthority> result =
                converter.convert(jwt);

        assertNotNull(result);
        assertTrue(hasAuthority(result, "ROLE_ADMIN"));
        assertTrue(hasAuthority(result, "ROLE_ALL_USERS"));
    }

    @Test
    void shouldKeepExistingRolePrefix() {

        Jwt jwt = jwt(
                Arrays.asList(
                        "ROLE_ADMIN",
                        "ROLE_USER"
                )
        );

        Collection<GrantedAuthority> result = converter.convert(jwt);

        assertNotNull(result);
        assertTrue(hasAuthority(result, "ROLE_ADMIN"));
        assertTrue(hasAuthority(result, "ROLE_USER"));
        assertFalse(hasAuthority(result, "ROLE_ROLE_ADMIN"));
    }

    @Test
    void shouldHandleNullGroups() {

        Jwt jwt = new Jwt(
                "token",
                Instant.now(),
                Instant.now().plusSeconds(3600),
                Collections.singletonMap("alg", "none"),
                Collections.singletonMap(
                        "sub",
                        "user"
                )
        );

        Collection<GrantedAuthority> result = converter.convert(jwt);

        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    @Test
    void shouldIgnoreBlankGroups() {

        Jwt jwt = jwt(
                Arrays.asList(
                        "ADMIN",
                        "",
                        " ",
                        "USER"
                )
        );

        Collection<GrantedAuthority> result = converter.convert(jwt);

        assertNotNull(result);
        assertTrue(hasAuthority(result, "ROLE_ADMIN"));
        assertTrue(hasAuthority(result, "ROLE_USER"));
    }

    @Test
    void shouldKeepDefaultScopeAuthorities() {

        Jwt jwt = new Jwt(
                "token",
                Instant.now(),
                Instant.now().plusSeconds(3600),
                Collections.singletonMap(
                        "alg",
                        "none"
                ),
                Map.of(
                        "sub",
                        "user",
                        "scope",
                        "read write",
                        "groups",
                        List.of("ADMIN")
                )
        );

        Collection<GrantedAuthority> result = converter.convert(jwt);

        assertNotNull(result);
        assertTrue(hasAuthority(result, "ROLE_ADMIN"));
        assertTrue(hasAuthority(result, "SCOPE_read"));
        assertTrue(hasAuthority(result, "SCOPE_write"));
    }

    private Jwt jwt(List<String> groups) {

        return new Jwt(
                "token",
                Instant.now(),
                Instant.now().plusSeconds(3600),
                Collections.singletonMap(
                        "alg",
                        "none"
                ),
                Map.of(
                        "sub",
                        "user",
                        "groups",
                        groups
                )
        );
    }

    private boolean hasAuthority(
            Collection<GrantedAuthority> authorities,
            String authority) {

        return authorities.stream()
                .anyMatch(item -> authority.equals(item.getAuthority()));
    }
}