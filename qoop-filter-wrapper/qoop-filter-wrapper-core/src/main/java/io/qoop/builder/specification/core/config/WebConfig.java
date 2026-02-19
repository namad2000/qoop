package io.qoop.builder.specification.core.config;

import io.qoop.builder.specification.core.FilterWrapperConverter;
import io.qoop.builder.specification.core.SortWrapperConverter;
import org.springframework.context.annotation.Configuration;
import org.springframework.format.FormatterRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    private final FilterWrapperConverter filterWrapperConverter;
    private final SortWrapperConverter sortWrapperConverter;

    public WebConfig(FilterWrapperConverter filterWrapperConverter, SortWrapperConverter sortWrapperConverter) {
        this.filterWrapperConverter = filterWrapperConverter;
        this.sortWrapperConverter = sortWrapperConverter;
    }

    @Override
    public void addFormatters(FormatterRegistry registry) {
        registry.addConverter(filterWrapperConverter);
        registry.addConverter(sortWrapperConverter);
    }
}