package io.qoop.security.resource.config;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.core.DefaultOAuth2AuthenticatedPrincipal;
import org.springframework.security.oauth2.core.OAuth2AuthenticatedPrincipal;
import org.springframework.security.oauth2.server.resource.introspection.OpaqueTokenIntrospector;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

public class GroupsOpaqueTokenIntrospector implements OpaqueTokenIntrospector {
    private final OpaqueTokenIntrospector delegate;

    public GroupsOpaqueTokenIntrospector(OpaqueTokenIntrospector delegate) {
        this.delegate = delegate;
    }

    @Override
    public OAuth2AuthenticatedPrincipal introspect(String token) {

        OAuth2AuthenticatedPrincipal principal = delegate.introspect(token);

        List<GrantedAuthority> authorities = new ArrayList<>(principal.getAuthorities());

        Object groupsAttribute = principal.getAttribute("groups");

        if (groupsAttribute != null) {
            for (String group : extractGroups(groupsAttribute)) {

                String normalized = group.trim();

                if (normalized.isEmpty()) {
                    continue;
                }

                if (normalized.startsWith("ROLE_")) {
                    authorities.add(new SimpleGrantedAuthority(normalized));
                } else {
                    authorities.add(new SimpleGrantedAuthority("ROLE_" + normalized));
                }
            }
        }

        return new DefaultOAuth2AuthenticatedPrincipal(
                principal.getName(),
                principal.getAttributes(),
                authorities
        );
    }

    private List<String> extractGroups(Object value) {

        List<String> groups = new ArrayList<>();

        if (value instanceof Collection<?>) {
            for (Object item : (Collection<?>) value) {
                if (item != null) {
                    groups.add(item.toString());
                }
            }

            return groups;
        }

        String[] values = value.toString().split(",");

        groups.addAll(Arrays.asList(values));

        return groups;
    }
}