package io.qoop.feign.config;

import feign.RequestInterceptor;
import io.qoop.feign.ServiceUrlResolver;
import io.qoop.feign.SmartFeignInterceptor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ConditionalFeignConfig {

    @Bean
    @ConditionalOnProperty(name = "feign.discovery.enabled", havingValue = "true")
    public RequestInterceptor smartFeignInterceptor(ServiceUrlResolver urlResolver) {
        return new SmartFeignInterceptor(urlResolver);
    }
}