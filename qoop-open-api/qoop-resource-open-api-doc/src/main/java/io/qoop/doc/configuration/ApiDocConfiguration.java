package io.qoop.doc.configuration;

import io.qoop.properties.factory.YamlPropertySourceFactory;
import io.qoop.security.api.User;
import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.media.StringSchema;
import io.swagger.v3.oas.models.parameters.HeaderParameter;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.servers.Server;
import org.springdoc.core.customizers.OperationCustomizer;
import org.springdoc.core.customizers.ParameterCustomizer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;

/**
 * @author Davood Akbari - 1404
 * daak1365@gmail.com
 * daak1365@yahoo.com
 * 09125188694
 */

@Configuration
@PropertySource(value = "classpath:api-doc.yml", factory = YamlPropertySourceFactory.class)
public class ApiDocConfiguration {
    @Value("${doc.title}")
    private String title;

    @Value("${doc.version}")
    private String version;

    @Value("${doc.description}")
    private String description;

    @Value("${doc.server.url}")
    private String serverUrl;

    @Bean
    public OpenAPI openAPI() {
        final String securitySchemeName = "bearerAuth";

        return new OpenAPI()
                .info(new Info().title(title).version(version).description(description))
                .addServersItem(new Server().url(serverUrl).description("Base Path"))

                // Definition of JWT Bearer authentication method.
                .addSecurityItem(new SecurityRequirement().addList(securitySchemeName))
                .components(
                        new Components()
                                .addSecuritySchemes(
                                        securitySchemeName,
                                        new SecurityScheme()
                                                .name(securitySchemeName)
                                                .type(SecurityScheme.Type.HTTP)
                                                .scheme("bearer")
                                                .bearerFormat("JWT")
                                                .in(SecurityScheme.In.HEADER)
                                                .description("Enter JWT token starting with 'Bearer '")));
    }

    @Bean
    public OperationCustomizer addAcceptLanguageHeader() {
        return (operation, handlerMethod) -> {
            operation.addParametersItem(
                    new HeaderParameter()
                            .name("Accept-Language")
                            .description("Language preference for response messages")
                            .required(false)
                            .schema(new StringSchema()._default("fa-IR"))
                            .example("fa-IR")
            );

            return operation;
        };
    }

    @Bean
    public ParameterCustomizer hideCurrentUserParam() {
        return (parameterModel, methodParameter) -> {
            if (User.class.isAssignableFrom(methodParameter.getParameterType())) {
                return null;
            }

            return parameterModel;
        };
    }
}
