package io.qoop.security.resource.user;


import io.qoop.security.api.User;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.experimental.SuperBuilder;

@Getter
@SuperBuilder
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
