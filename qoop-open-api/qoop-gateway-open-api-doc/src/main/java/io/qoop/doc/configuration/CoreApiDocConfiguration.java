package io.qoop.doc.configuration;

import io.qoop.properties.factory.YamlPropertySourceFactory;
import org.springdoc.core.properties.AbstractSwaggerUiConfigProperties;
import org.springdoc.core.properties.SwaggerUiConfigProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.PropertySource;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * @author Davood Akbari - 1404
 * daak1365@gmail.com
 * daak1365@yahoo.com
 * 09125188694
 */

@Configuration
@PropertySource(value = "classpath:core-api-doc.yml", factory = YamlPropertySourceFactory.class)
public class CoreApiDocConfiguration {
    private final SwaggerProperties swaggerProperties;

    public CoreApiDocConfiguration(SwaggerProperties swaggerProperties) {
        this.swaggerProperties = swaggerProperties;
    }

    @Bean
    @Primary
    public SwaggerUiConfigProperties swaggerUiConfigProperties() {
        SwaggerUiConfigProperties configProperties = new SwaggerUiConfigProperties();

        List<SwaggerProperties.SwaggerUrl> urls = swaggerProperties.getUrls();

        Set<AbstractSwaggerUiConfigProperties.SwaggerUrl> swaggerUrls = new HashSet<>();

        for (SwaggerProperties.SwaggerUrl url : urls) {
            swaggerUrls.add(new AbstractSwaggerUiConfigProperties.SwaggerUrl("Online-" + url.getName(), url.getUrl(), url.getName()));
        }

        configProperties.setUrls(swaggerUrls);
        return configProperties;
    }
}
