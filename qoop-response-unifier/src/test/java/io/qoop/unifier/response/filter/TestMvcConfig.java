package io.qoop.unifier.response.filter;

import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;

@Configuration
@EnableWebMvc
@ComponentScan(basePackageClasses = {
        BodyRewrite.class,
        TestController.class,
        TestCorrelationIdFilter.class
})
class TestMvcConfig {
}
