package io.qoop.security.resource;

import io.qoop.security.api.AuditorProvider;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Profile;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Primary
@Component
@Profile("!c-gateway-security")
@ConditionalOnClass(name = "org.springframework.security.core.context.SecurityContextHolder")
public class SecurityAuditorAware implements AuditorProvider {

    @Override
    public Optional<String> getCurrentAuditor() {
        // Get authentication from SecurityContextHolder (ThreadLocal)
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null || !authentication.isAuthenticated()) {
            return Optional.of("system");
        }

        // Return the username of the logged-in user
        return Optional.ofNullable(authentication.getName());
    }
}
