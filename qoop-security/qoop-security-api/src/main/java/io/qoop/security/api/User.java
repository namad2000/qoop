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

    String getOrganization();

    String[] getRoles();

    boolean isVirtual();

    boolean isAuthenticated();
}
