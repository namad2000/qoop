package io.qoop.jpa.config;

import io.qoop.security.api.AuditorProvider;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
public class SystemAuditorProvider implements AuditorProvider {

    @Override
    public Optional<String> getCurrentAuditor() {
        return Optional.of("SYSTEM");
    }
}
