package io.qoop.security.resource.config;

import io.qoop.properties.factory.YamlPropertySourceFactory;
import io.qoop.security.api.PrefixPath;
import io.qoop.security.config.SecurityProperties;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity // Allows @PreAuthorize on Controller methods
@PropertySource(value = "classpath:resource-security.yml", factory = YamlPropertySourceFactory.class)
@RequiredArgsConstructor
public class SecurityConfiguration {

    private final SecurityProperties securityProps;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) {
        http
                .authorizeHttpRequests(authorize -> authorize
                        .requestMatchers(PrefixPath.INTERNAL.concat("/**")).permitAll() // INTERNAL endpoints
                        .requestMatchers(securityProps.getWhitelistUrls().toArray(new String[0])).permitAll()
                        .anyRequest().authenticated()             // Everything else requires login
                )
                .oauth2ResourceServer(oauth2 -> oauth2
                        .jwt(Customizer.withDefaults())           // Enable JWT validation using JWKS
                );

        return http.build();
    }
}
