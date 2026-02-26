package io.qoop.security.resource.user;

import io.qoop.security.api.User;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@AllArgsConstructor
public class AuthenticatedUser implements User {
    private final String username;
    private String organization;
    private String[] roles;
    private boolean virtual;
    boolean authenticated;

    @Override
    public String getName() {
        return username;
    }
}