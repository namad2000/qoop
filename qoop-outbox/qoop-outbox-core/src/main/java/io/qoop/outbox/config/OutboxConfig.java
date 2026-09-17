package io.qoop.outbox.config;

import io.qoop.properties.factory.YamlPropertySourceFactory;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;

@Configuration
@EnableConfigurationProperties(OutboxProperties.class)
@PropertySource(value = "classpath:qoop-outbox.yml", factory = YamlPropertySourceFactory.class)
public class OutboxConfig {
}