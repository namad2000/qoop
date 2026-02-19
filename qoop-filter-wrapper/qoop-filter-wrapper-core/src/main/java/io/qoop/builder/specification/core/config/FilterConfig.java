package io.qoop.builder.specification.core.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.qoop.builder.specification.api.model.Sort;
import io.qoop.builder.specification.core.SortMixin;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class FilterConfig {

    @Bean
    @ConditionalOnClass(Sort.class)
    public ObjectMapper objectMapper() {
        ObjectMapper mapper = new ObjectMapper();
        mapper.findAndRegisterModules();
        mapper.addMixIn(Sort.class, SortMixin.class);
        return mapper;
    }
}