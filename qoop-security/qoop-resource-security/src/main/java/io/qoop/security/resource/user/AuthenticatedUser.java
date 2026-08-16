package io.qoop.security.resource.user;

import io.qoop.security.api.User;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Builder
@AllArgsConstructor
public class AuthenticatedUser implements User {
    private final String username;

    @Getter
    private String organization;

    @Getter
    private String[] roles;

    private Boolean virtual;
    private Boolean authenticated;

    @Override
    public String getName() {
        return username;
    }

    public Boolean isVirtual() {
        return virtual;
    }

    public Boolean isAuthenticated() {
        return authenticated;
    }
}