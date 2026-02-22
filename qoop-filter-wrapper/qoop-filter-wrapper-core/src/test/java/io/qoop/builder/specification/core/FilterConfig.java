package io.qoop.builder.specification.core;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.qoop.builder.specification.api.model.Sort;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

@Configuration
public class FilterConfig {

    @Bean
    @Primary
    @ConditionalOnClass(Sort.class)
    public ObjectMapper objectMapper() {
        return new ObjectMapper();
    }
}