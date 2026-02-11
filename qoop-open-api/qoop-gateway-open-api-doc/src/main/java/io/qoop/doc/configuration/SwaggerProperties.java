package io.qoop.doc.configuration;

/**
 * @author Davood Akbari - 1404
 * daak1365@gmail.com
 * daak1365@yahoo.com
 * 09125188694
 */

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.List;

@Setter
@Getter
@Component
@ConfigurationProperties(prefix = "doc.swagger-ui")
public class SwaggerProperties {

    private List<SwaggerUrl> urls;

    @Setter
    @Getter
    public static class SwaggerUrl {
        private String name;
        private String url;
    }
}
