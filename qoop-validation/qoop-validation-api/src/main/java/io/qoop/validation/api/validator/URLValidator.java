package io.qoop.validation.api.validator;

import io.qoop.fault.handler.api.exception.DomainValidationException;
import io.qoop.validation.api.URL;

import java.net.URI;

import static io.qoop.validation.api.exception.ValidationExceptionCode.INVALID_URL;

/**
 * @author Davood Akbari - 1404
 * daak1365@gmail.com
 * daak1365@yahoo.com
 * 09125188694
 */
public class URLValidator implements AnnotationValidator<CharSequence, URL> {

    @Override
    public void validate(CharSequence value, URL annotation, String paramName) {
        if (value == null || value.length() == 0) {
            return;
        }

        String urlString = value.toString().trim();

        try {
            URI uri = new URI(urlString);

            if (uri.getScheme() == null || uri.getHost() == null) {
                throw DomainValidationException.withParams(INVALID_URL, paramName, paramName);
            }

            if (annotation.protocol() != null && !annotation.protocol().isEmpty()) {
                if (!annotation.protocol().equalsIgnoreCase(uri.getScheme())) {
                    throw DomainValidationException.withParams(INVALID_URL, paramName, paramName);
                }
            }

            if (annotation.host() != null && !annotation.host().isEmpty()) {
                if (!annotation.host().equalsIgnoreCase(uri.getHost())) {
                    throw DomainValidationException.withParams(INVALID_URL, paramName, paramName);
                }
            }

            if (annotation.port() != -1) {
                int effectivePort = uri.getPort() != -1 ? uri.getPort() : getDefaultPort(uri.getScheme());
                if (effectivePort != annotation.port()) {
                    throw DomainValidationException.withParams(INVALID_URL, paramName, paramName);
                }
            }

        } catch (DomainValidationException e) {
            throw e;
        } catch (Exception e) {
            throw DomainValidationException.withParams(INVALID_URL, paramName, paramName);
        }
    }

    private int getDefaultPort(String scheme) {
        if ("http".equalsIgnoreCase(scheme)) {
            return 80;
        }
        if ("https".equalsIgnoreCase(scheme)) {
            return 443;
        }
        return -1;
    }
}