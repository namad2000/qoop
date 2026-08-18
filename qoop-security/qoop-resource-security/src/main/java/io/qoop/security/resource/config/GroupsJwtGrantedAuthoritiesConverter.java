package io.qoop.security.resource.config;

import org.springframework.core.convert.converter.Converter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtGrantedAuthoritiesConverter;

import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class GroupsJwtGrantedAuthoritiesConverter
        implements Converter<Jwt, Collection<GrantedAuthority>> {

    private final JwtGrantedAuthoritiesConverter delegate = new JwtGrantedAuthoritiesConverter();

    @Override
    public Collection<GrantedAuthority> convert(Jwt jwt) {

        Collection<GrantedAuthority> defaultAuthorities = delegate.convert(jwt);

        Set<GrantedAuthority> authorities = new HashSet<>(defaultAuthorities);

        List<String> groups = jwt.getClaimAsStringList("groups");

        if (groups != null) {
            groups.stream()
                    .map(String::trim)
                    .filter(group -> !group.isEmpty())
                    .map(this::toAuthority)
                    .forEach(authorities::add);
        }

        return authorities;
    }

    private GrantedAuthority toAuthority(String group) {
        if (group.startsWith("ROLE_")) {
            return new SimpleGrantedAuthority(group);
        }

        return new SimpleGrantedAuthority("ROLE_" + group);
    }
}