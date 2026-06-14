package io.qoop.feign;

import feign.RequestInterceptor;
import feign.RequestTemplate;
import org.springframework.stereotype.Component;

@Component
public class SmartSmsInterceptor implements RequestInterceptor {

    private final SmsUrlResolver urlResolver;

    public SmartSmsInterceptor(SmsUrlResolver urlResolver) {
        this.urlResolver = urlResolver;
    }

    @Override
    public void apply(RequestTemplate template) {
        String newBaseUrl = urlResolver.resolveBaseUrl();
        String path = template.path();
        String query = template.queryLine();

        String finalUrl = newBaseUrl;

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