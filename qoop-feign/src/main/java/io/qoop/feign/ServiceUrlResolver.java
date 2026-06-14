package io.qoop.feign;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Service
public class ServiceUrlResolver {

    private final RestTemplate restTemplate;

    @Value("${feign.discovery.enabled:false}")
    private boolean discoveryEnabled;

    @Value("${discovery.service.host:localhost}")
    private String discoveryHost;

    @Value("${discovery.service.port:8500}")
    private int discoveryPort;

    public ServiceUrlResolver(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    /**
     * Resolves the URL. If discovery is enabled, fetches from discovery service.
     * Otherwise, returns null to indicate direct connection is needed.
     */
    public String resolveUrlForService(String serviceName) {
        if (!discoveryEnabled) {
            return null;
        }

        try {
            String discoveryUrl = String.format(
                    "http://%s:%d/v1/catalog/service/%s",
                    discoveryHost,
                    discoveryPort,
                    serviceName
            );

            String discoveredUrl = restTemplate.getForObject(discoveryUrl, String.class);

            if (discoveredUrl != null && !discoveredUrl.isEmpty()) {
                // Extract IP and Port from Consul response if necessary
                // Assuming the response contains the necessary info or a direct URL
                return discoveredUrl;
            }
        } catch (Exception e) {
            // Fallback to null (direct connection)
        }

        return null;
    }
}