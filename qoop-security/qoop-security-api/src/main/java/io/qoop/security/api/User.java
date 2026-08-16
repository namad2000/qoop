package io.qoop.security.api;

/**
 * Author: davood akbari
 * Email: daak1365@gmail.com
 * Created: 1/4/2026 4:05 PM
 * Package: io.qoop.domain.model
 */

public interface User {
    String SYSTEM_USER = "anonymous";

    default String getName() {
        return SYSTEM_USER;
    }

    default String getOrganization() {
        return SYSTEM_USER;
    }

    default String[] getRoles() {
        return new String[]{SYSTEM_USER};
    }

    default Boolean isVirtual() {
        return false;
    }

    default Boolean isAuthenticated() {
        return true;
    }
}
