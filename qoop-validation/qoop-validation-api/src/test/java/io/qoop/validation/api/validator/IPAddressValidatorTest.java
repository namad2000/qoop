package io.qoop.validation.api.validator;

import io.qoop.fault.handler.api.exception.DomainValidationException;
import io.qoop.validation.api.IPAddress;
import io.qoop.validation.api.exception.ValidationExceptionCode;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.lang.annotation.Annotation;

import static org.junit.jupiter.api.Assertions.*;

class IPAddressValidatorTest {

    private IPAddressValidator validator;

    @BeforeEach
    void setUp() {
        validator = new IPAddressValidator();
    }

    private IPAddress createIPAddress(IPAddress.Type type) {
        return new IPAddress() {
            @Override
            public Class<? extends Annotation> annotationType() {
                return IPAddress.class;
            }

            @Override
            public Type type() {
                return type;
            }
        };
    }

    @Test
    @DisplayName("Should pass when value is null or empty")
    void validate_NullOrEmptyValue_Success() {
        IPAddress annotation = createIPAddress(IPAddress.Type.ANY);
        assertDoesNotThrow(() -> validator.validate(null, annotation, "clientIp"));
        assertDoesNotThrow(() -> validator.validate("", annotation, "clientIp"));
    }

    @ParameterizedTest
    @ValueSource(strings = {
            "192.168.1.1",
            "127.0.0.1",
            "10.0.0.255",
            "2001:0db8:85a3:0000:0000:8a2e:0370:7334",
            "fe80::1"
    })
    @DisplayName("Should pass when IP is valid (Type ANY)")
    void validate_ValidIPAddressAny_Success(String input) {
        IPAddress annotation = createIPAddress(IPAddress.Type.ANY);
        assertDoesNotThrow(() -> validator.validate(input, annotation, "clientIp"));
    }

    @Test
    @DisplayName("Should validate specific IP types correctly")
    void validate_SpecificIPType() {
        IPAddress ipv4Only = createIPAddress(IPAddress.Type.IPv4);
        IPAddress ipv6Only = createIPAddress(IPAddress.Type.IPv6);

        assertDoesNotThrow(() -> validator.validate("10.0.0.1", ipv4Only, "clientIp"));
        assertThrows(DomainValidationException.class, () -> validator.validate("fe80::1", ipv4Only, "clientIp"));

        assertDoesNotThrow(() -> validator.validate("fe80::1", ipv6Only, "clientIp"));
        assertThrows(DomainValidationException.class, () -> validator.validate("10.0.0.1", ipv6Only, "clientIp"));
    }

    @ParameterizedTest
    @ValueSource(strings = {
            "256.0.0.1",
            "192.168.1",
            "invalid-ip",
            "123.456.789.000"
    })
    @DisplayName("Should throw exception when IP address format is invalid")
    void validate_InvalidIPAddress_ThrowsDomainValidationException(String input) {
        IPAddress annotation = createIPAddress(IPAddress.Type.ANY);

        DomainValidationException exception = assertThrows(
                DomainValidationException.class,
                () -> validator.validate(input, annotation, "clientIp")
        );

        assertEquals(ValidationExceptionCode.INVALID_IP_ADDRESS, exception.getCode());
    }
}