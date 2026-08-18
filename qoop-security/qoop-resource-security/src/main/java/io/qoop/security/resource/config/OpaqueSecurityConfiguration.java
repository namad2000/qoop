package io.qoop.security.resource.config;

import io.qoop.security.api.PrefixPath;
import io.qoop.security.config.SecurityProperties;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.oauth2.server.resource.introspection.OpaqueTokenIntrospector;
import org.springframework.security.oauth2.server.resource.web.BearerTokenAuthenticationEntryPoint;
import org.springframework.security.oauth2.server.resource.web.access.BearerTokenAccessDeniedHandler;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.LoginUrlAuthenticationEntryPoint;


@Configuration
@EnableWebSecurity
@EnableMethodSecurity(jsr250Enabled = true)
@Profile("verify")
@RequiredArgsConstructor
public class OpaqueSecurityConfiguration {

    private final SecurityProperties securityProps;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) {
        SecurityProperties.OAuth2 oauth2 = securityProps.getOpaque().getOauth2();

        http
                .csrf(AbstractHttpConfigurer::disable)
                .sessionManagement(session -> session
                        .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                )
                .authorizeHttpRequests(authorize -> authorize
                        .requestMatchers(PrefixPath.INTERNAL.concat("/**")).permitAll()
                        .requestMatchers(securityProps.getWhitelistUrls().toArray(new String[0])).permitAll()
                        .anyRequest().authenticated()
                )
                .oauth2ResourceServer(oauth2Server ->
                        oauth2Server
                                .opaqueToken(opaque -> opaque
                                        .introspectionUri(oauth2.getVerifyEndpoint())
                                        .introspectionClientCredentials(
                                                oauth2.getClientId(),
                                                oauth2.getClientSecret()
                                        )
                                        .introspector(
                                                groupsOpaqueTokenIntrospector()
                                        )
                                )
                )
                .exceptionHandling(exception -> exception
                        .authenticationEntryPoint(new BearerTokenAuthenticationEntryPoint())
                        .accessDeniedHandler(new BearerTokenAccessDeniedHandler())
                );

        // If authEndpoint is provided, redirect to auth-server; otherwise return 401/403
        if (oauth2.getAuthEndpoint() != null && !oauth2.getAuthEndpoint().isBlank()) {
            http.exceptionHandling(exception -> exception
                    .authenticationEntryPoint(new LoginUrlAuthenticationEntryPoint(oauth2.getAuthEndpoint()))
            );
        } else {
            http.exceptionHandling(exception -> exception
                    .authenticationEntryPoint(new BearerTokenAuthenticationEntryPoint())
                    .accessDeniedHandler(new BearerTokenAccessDeniedHandler())
            );
        }

        return http.build();
    }

    private OpaqueTokenIntrospector groupsOpaqueTokenIntrospector() {
        OpaqueTokenIntrospector delegate = createDefaultIntrospector();
        return new GroupsOpaqueTokenIntrospector(delegate);
    }

    private OpaqueTokenIntrospector createDefaultIntrospector() {
        return token -> {
            throw new UnsupportedOperationException(
                    "Default OpaqueTokenIntrospector must be provided by Spring Security"
            );
        };
    }
}