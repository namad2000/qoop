package io.qoop.security.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import java.util.ArrayList;
import java.util.List;

@Setter
@Getter
@Configuration
@ConfigurationProperties(prefix = "security")
public class SecurityProperties {

    private List<String> whitelistUrls = new ArrayList<>();
    private PathContain pathContain = new PathContain();
    private Opaque opaque = new Opaque();

    @Setter
    @Getter
    public static class PathContain {
        private String keyword = "";
        private int rejectStatus = 403;
    }

    @Setter
    @Getter
    public static class Opaque {
        private OAuth2 oauth2 = new OAuth2();
    }

    @Setter
    @Getter
    public static class OAuth2 {
        private String authEndpoint;
        private String verifyEndpoint;
        private String clientId = "default-client";
        private String clientSecret = "default-secret";
    }
}