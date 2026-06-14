package io.qoop.feign;

import feign.RequestTemplate;
import feign.Target;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class SmartFeignInterceptorTest {

    @Mock
    private ServiceUrlResolver urlResolver;

    @InjectMocks
    private SmartFeignInterceptor interceptor;

    private RequestTemplate template;
    private Target<?> mockTarget;

    @BeforeEach
    void setUp() {
        template = new RequestTemplate();
        mockTarget = new Target.HardCodedTarget<>(String.class, "user-service", "http://default-user-service:8080");
        template.feignTarget(mockTarget);
    }

    @Test
    void apply_WhenDiscoveryReturnsUrl_ShouldUpdateTarget() {
        // Arrange
        String resolvedUrl = "http://discovered-user-service:9090";
        String path = "/users/1";
        String query = "?expand=true";

        when(urlResolver.resolveUrlForService("user-service")).thenReturn(resolvedUrl);
        template.uri(path + query);

        // Act
        interceptor.apply(template);

        // Assert
        verify(urlResolver).resolveUrlForService("user-service");
    }

    @Test
    void apply_WhenDiscoveryReturnsNull_ShouldNotUpdateTarget() {
        // Arrange
        String path = "/users/1";
        String query = "?expand=true";

        when(urlResolver.resolveUrlForService("user-service")).thenReturn(null);
        template.uri(path + query);

        // Act
        interceptor.apply(template);

        // Assert
        verify(urlResolver).resolveUrlForService("user-service");
    }

    @Test
    void apply_WhenTargetIsNull_ShouldHandleGracefully() {
        // Arrange
        template.feignTarget(null);
        String path = "/users/1";
        template.uri(path);

        // Act
        interceptor.apply(template);

        // Assert
        verify(urlResolver).resolveUrlForService("unknown-service");
    }
}