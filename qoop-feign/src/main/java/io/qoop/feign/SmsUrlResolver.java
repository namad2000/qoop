package io.qoop.feign;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Service
public class SmsUrlResolver {

    private final RestTemplate restTemplate;

    @Value("${sms.baseUrl}")
    private String defaultBaseUrl;

    @Value("${sms.discovery.enabled:false}")
    private boolean discoveryEnabled;

    @Value("${sms.discovery.url:http://localhost:8081}")
    private String discoveryServiceUrl;

    public SmsUrlResolver(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    public String resolveBaseUrl() {
        if (!discoveryEnabled) {
            return defaultBaseUrl;
        }
        try {
            String discoveredUrl = restTemplate.getForObject(discoveryServiceUrl + "/resolve/sms", String.class);
            if (discoveredUrl != null && !discoveredUrl.isEmpty()) {
                return discoveredUrl;
            }
        } catch (Exception e) {
            // Fallback to default URL if discovery fails
        }
        return defaultBaseUrl;
    }
}