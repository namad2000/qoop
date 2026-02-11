package ir.online.commons.filter.bean.core;

import org.junit.jupiter.api.Test;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Author: davood akbari
 * Email: daak1365@gmail.com
 * Created: 12/28/2025 4:02 PM
 * Package: ir.online.commons.configuration
 */

class BeanFilterConfigurationTest {

    @Test
    void testBeansAreLoaded() {
        // Create Spring context using the AppConfig class
        ApplicationContext context = new AnnotationConfigApplicationContext(BeanFilterConfiguration.class);

        // Verify that beans annotated with @DomainMapper are loaded
        SomeDomainMapper mapperBean = context.getBean(SomeDomainMapper.class);
        assertThat(mapperBean).isNotNull();

        // Verify that beans annotated with @DomainService are loaded
        SomeDomainService serviceBean = context.getBean(SomeDomainService.class);
        assertThat(serviceBean).isNotNull();

        // Verify that beans annotated with @UseCaseService are loaded
        SomeUseCaseService useCaseBean = context.getBean(SomeUseCaseService.class);
        assertThat(useCaseBean).isNotNull();
    }
}
