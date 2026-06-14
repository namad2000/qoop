package io.qoop.feign;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.client.RestTemplate;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ServiceUrlResolverTest {

    @Mock
    private RestTemplate restTemplate;

    private ServiceUrlResolver resolver;

    @BeforeEach
    void setUp() {
        resolver = new ServiceUrlResolver(restTemplate);
    }

    @Test
    void resolveUrlForService_WhenDiscoveryDisabled_ShouldReturnNull() {
        // Arrange
        ReflectionTestUtils.setField(resolver, "discoveryEnabled", false);

        // Act
        String result = resolver.resolveUrlForService("sms");

        // Assert
        assertEquals(null, result);
    }

    @Test
    void resolveUrlForService_WhenDiscoveryEnabled_ShouldReturnDiscoveredUrl() {
        // Arrange
        ReflectionTestUtils.setField(resolver, "discoveryEnabled", true);
        ReflectionTestUtils.setField(resolver, "discoveryHost", "localhost");
        ReflectionTestUtils.setField(resolver, "discoveryPort", 8500);

        String expectedUrl = "http://192.168.1.10:8080";
        when(restTemplate.getForObject("http://localhost:8500/v1/catalog/service/sms", String.class))
                .thenReturn(expectedUrl);

        // Act
        String result = resolver.resolveUrlForService("sms");

        // Assert
        assertEquals(expectedUrl, result);
    }

    @Test
    void resolveUrlForService_WhenDiscoveryFails_ShouldReturnNull() {
        // Arrange
        ReflectionTestUtils.setField(resolver, "discoveryEnabled", true);
        ReflectionTestUtils.setField(resolver, "discoveryHost", "localhost");
        ReflectionTestUtils.setField(resolver, "discoveryPort", 8500);

        when(restTemplate.getForObject("http://localhost:8500/v1/catalog/service/sms", String.class))
                .thenThrow(new RuntimeException("Service Unavailable"));

        // Act
        String result = resolver.resolveUrlForService("sms");

        // Assert
        assertEquals(null, result);
    }

    @Test
    void resolveUrlForService_WhenDiscoveryReturnsEmpty_ShouldReturnNull() {
        // Arrange
        ReflectionTestUtils.setField(resolver, "discoveryEnabled", true);
        ReflectionTestUtils.setField(resolver, "discoveryHost", "localhost");
        ReflectionTestUtils.setField(resolver, "discoveryPort", 8500);

        when(restTemplate.getForObject("http://localhost:8500/v1/catalog/service/sms", String.class))
                .thenReturn("");

        // Act
        String result = resolver.resolveUrlForService("sms");

        // Assert
        assertEquals(null, result);
    }
}