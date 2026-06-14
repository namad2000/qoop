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
class SmsUrlResolverTest {

    @Mock
    private RestTemplate restTemplate;

    private SmsUrlResolver resolver;

    @BeforeEach
    void setUp() {
        resolver = new SmsUrlResolver(restTemplate);
    }

    @Test
    void resolveBaseUrl_WhenDiscoveryDisabled_ShouldReturnDefaultUrl() {
        // Arrange
        ReflectionTestUtils.setField(resolver, "discoveryEnabled", false);
        ReflectionTestUtils.setField(resolver, "defaultBaseUrl", "http://default.com");

        // Act
        String result = resolver.resolveBaseUrl();

        // Assert
        assertEquals("http://default.com", result);
    }

    @Test
    void resolveBaseUrl_WhenDiscoveryEnabled_ShouldReturnDiscoveredUrl() {
        // Arrange
        ReflectionTestUtils.setField(resolver, "discoveryEnabled", true);
        ReflectionTestUtils.setField(resolver, "defaultBaseUrl", "http://default.com");
        ReflectionTestUtils.setField(resolver, "discoveryServiceUrl", "http://discovery.com");

        String expectedDiscoveredUrl = "http://new-sms-service.com";
        when(restTemplate.getForObject("http://discovery.com/resolve/sms", String.class))
                .thenReturn(expectedDiscoveredUrl);

        // Act
        String result = resolver.resolveBaseUrl();

        // Assert
        assertEquals(expectedDiscoveredUrl, result);
    }

    @Test
    void resolveBaseUrl_WhenDiscoveryFails_ShouldReturnDefaultUrl() {
        // Arrange
        ReflectionTestUtils.setField(resolver, "discoveryEnabled", true);
        ReflectionTestUtils.setField(resolver, "defaultBaseUrl", "http://default.com");
        ReflectionTestUtils.setField(resolver, "discoveryServiceUrl", "http://discovery.com");

        // Simulate discovery service error
        when(restTemplate.getForObject("http://discovery.com/resolve/sms", String.class))
                .thenThrow(new RuntimeException("Service Unavailable"));

        // Act
        String result = resolver.resolveBaseUrl();

        // Assert
        assertEquals("http://default.com", result);
    }
}