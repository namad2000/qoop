package io.qoop.feign;

import feign.RequestInterceptor;
import feign.RequestTemplate;
import feign.Target;
import org.springframework.stereotype.Component;

@Component
public class SmartFeignInterceptor implements RequestInterceptor {

    private final ServiceUrlResolver urlResolver;

    public SmartFeignInterceptor(ServiceUrlResolver urlResolver) {
        this.urlResolver = urlResolver;
    }

    @Override
    public void apply(RequestTemplate template) {
        String serviceName = extractServiceName(template);

        String resolvedUrl = urlResolver.resolveUrlForService(serviceName);

        if (resolvedUrl != null) {
            String path = template.path();
            String query = template.queryLine();

            String finalUrl = resolvedUrl;

            if (!path.isEmpty()) {
                if (!path.startsWith("/")) {
                    finalUrl += "/";
                }
                finalUrl += path;
            }

            if (!query.isEmpty()) {
                finalUrl += query;
            }

            template.target(finalUrl);
        }
    }

    /**
     * Extracts the service name from the RequestTemplate's Feign target.
     */
    private String extractServiceName(RequestTemplate template) {
        Target<?> target = template.feignTarget();
        if (target != null) {
            return target.name();
        }
        return "unknown-service";
    }
}