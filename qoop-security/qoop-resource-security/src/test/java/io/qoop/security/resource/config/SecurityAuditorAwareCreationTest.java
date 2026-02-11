package io.qoop.security.resource.config;

import io.qoop.security.resource.SecurityAuditorAware;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.FilteredClassLoader;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import org.springframework.security.core.context.SecurityContextHolder;

import static org.assertj.core.api.Assertions.assertThat;

class SecurityAuditorAwareCreationTest {

    // Removed AutoConfigurations.of(...) to prevent duplicate bean registration
    private final ApplicationContextRunner contextRunner = new ApplicationContextRunner();

    @Test
    void whenProfileIsNotGatewayAndClassExists_thenBeanIsCreated() {
        this.contextRunner
                .withPropertyValues("spring.profiles.active:test")
                // Register the class here to test @Component and conditions
                .withUserConfiguration(SecurityAuditorAware.class)
                .run(context -> {
                    assertThat(context).hasSingleBean(SecurityAuditorAware.class);
                    assertThat(context).hasBean("securityAuditorAware");
                });
    }

    @Test
    void whenProfileIsGateway_thenBeanIsNotCreated() {
        this.contextRunner
                .withPropertyValues("spring.profiles.active:c-gateway-security")
                .withUserConfiguration(SecurityAuditorAware.class)
                .run(context -> {
                    assertThat(context).doesNotHaveBean(SecurityAuditorAware.class);
                });
    }

    @Test
    void whenSecurityClassIsMissing_thenBeanIsNotCreated() {
        this.contextRunner
                .withClassLoader(new FilteredClassLoader(SecurityContextHolder.class))
                .withUserConfiguration(SecurityAuditorAware.class)
                .run(context -> {
                    assertThat(context).doesNotHaveBean(SecurityAuditorAware.class);
                });
    }
}
