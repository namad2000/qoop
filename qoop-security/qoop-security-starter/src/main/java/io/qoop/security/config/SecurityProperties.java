package io.qoop.security.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Setter
@Getter
@Configuration
@ConfigurationProperties(prefix = "security")
public class SecurityProperties {

    private List<String> whitelistUrls;
    private PathContain pathContain = new PathContain();

    @Setter
    @Getter
    public static class PathContain {
        private String keyword = "";
        private int rejectStatus = 403;
    }
}
