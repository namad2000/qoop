package io.qoop.validation.api.validator;

import io.qoop.fault.handler.api.exception.DomainValidationException;
import io.qoop.validation.api.URL;
import io.qoop.validation.api.exception.ValidationExceptionCode;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.lang.annotation.Annotation;

import static org.junit.jupiter.api.Assertions.*;

class URLValidatorTest {

    private URLValidator validator;

    @BeforeEach
    void setUp() {
        validator = new URLValidator();
    }

    private URL createURL(String protocol, String host, int port) {
        return new URL() {
            @Override
            public Class<? extends Annotation> annotationType() {
                return URL.class;
            }

            @Override
            public String protocol() {
                return protocol;
            }

            @Override
            public String host() {
                return host;
            }

            @Override
            public int port() {
                return port;
            }
        };
    }

    @Test
    @DisplayName("Should pass when value is null or empty")
    void validate_NullOrEmptyValue_Success() {
        URL annotation = createURL("", "", -1);
        assertDoesNotThrow(() -> validator.validate(null, annotation, "website"));
        assertDoesNotThrow(() -> validator.validate("", annotation, "website"));
    }

    @ParameterizedTest
    @ValueSource(strings = {
            "https://qoop.io",
            "http://localhost:8080/api/v1",
            "https://www.google.com/search?q=java"
    })
    @DisplayName("Should pass when URL format is valid")
    void validate_ValidURL_Success(String input) {
        URL annotation = createURL("", "", -1);
        assertDoesNotThrow(() -> validator.validate(input, annotation, "website"));
    }

    @Test
    @DisplayName("Should pass when URL matches specified constraints")
    void validate_URLWithSpecificConstraints_Success() {
        URL annotation = createURL("https", "qoop.io", 443);
        assertDoesNotThrow(() -> validator.validate("https://qoop.io:443/test", annotation, "website"));
    }

    @ParameterizedTest
    @ValueSource(strings = {
            "invalid-url",
            "http://",
            "ftp://:8080",
            "://qoop.io"
    })
    @DisplayName("Should throw exception when URL format is invalid")
    void validate_InvalidURL_ThrowsDomainValidationException(String input) {
        URL annotation = createURL("", "", -1);

        DomainValidationException exception = assertThrows(
                DomainValidationException.class,
                () -> validator.validate(input, annotation, "website")
        );

        assertEquals(ValidationExceptionCode.INVALID_URL, exception.getCode());
    }
}