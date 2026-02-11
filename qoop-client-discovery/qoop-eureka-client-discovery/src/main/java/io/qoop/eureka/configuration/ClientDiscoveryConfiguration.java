package io.qoop.eureka.configuration;

import io.qoop.properties.factory.YamlPropertySourceFactory;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;

/**
 * @author Davood Akbari - 1404
 * daak1365@gmail.com
 * daak1365@yahoo.com
 * 09125188694
 */

@Configuration
@EnableDiscoveryClient
@PropertySource(value = "classpath:client-discovery.yml", factory = YamlPropertySourceFactory.class)
public class ClientDiscoveryConfiguration {
}
