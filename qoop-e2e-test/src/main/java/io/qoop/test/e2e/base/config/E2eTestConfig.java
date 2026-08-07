package io.qoop.test.e2e.base.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Author: davood akbari
 * Email: daak1365@gmail.com
 * Created: 4/30/2026 5:50 PM
 * Package: ir.tamin.finance.hub.integration.config
 */

@Configuration
public class E2eTestConfig {

    @Bean
    public ObjectMapper objectMapper() {
        return new ObjectMapper();
    }
}
