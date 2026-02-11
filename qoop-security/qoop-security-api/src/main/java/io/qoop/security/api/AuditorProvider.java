package io.qoop.security.api;

import java.util.Optional;

public interface AuditorProvider {
    Optional<String> getCurrentAuditor();
}
