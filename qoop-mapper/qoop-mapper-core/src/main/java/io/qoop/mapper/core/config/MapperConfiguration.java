package io.qoop.mapper.core.config;

import io.qoop.mapper.api.shift.JsonEngine;
import io.qoop.mapper.core.shift.JacksonJsonEngine;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Author: davood akbari
 * Email: daak1365@gmail.com
 * Created: 1/28/2026 8:53 PM
 * Package: ir.online.commons.mapper.core.config
 */

@Configuration
public class MapperConfiguration {

    @Value("${shift.default.date-format:yyyy-MM-dd HH:mm:ss}")
    private String dateFormat;

    /**
     * Define the JsonEngine as a Bean so it can be used elsewhere if needed.
     */
    @Bean
    public JsonEngine jsonEngine() {
        return new JacksonJsonEngine(dateFormat);
    }
}
