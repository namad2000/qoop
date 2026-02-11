package io.qoop.security.resource.user;


import io.qoop.security.api.User;

public class AuthenticatedUser implements User {
    private final String username;

    public AuthenticatedUser(String username) {
        this.username = username;
    }

    @Override
    public String getName() {
        return username;
    }
}
