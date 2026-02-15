package io.qoop.feign;

import feign.Request;
import feign.Response;
import io.qoop.fault.handler.api.exception.DomainException;
import io.qoop.fault.handler.api.exception.ExceptionCode;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Collections;

import static org.assertj.core.api.Assertions.assertThat;

class QoopErrorDecoderTest {

    private QoopErrorDecoder decoder;

    @BeforeEach
    void setUp() {
        decoder = new QoopErrorDecoder();
    }

    @Test
    void should_decode_400_bad_request() {
        Response response = buildResponse(400, "bad request");

        Exception exception = decoder.decode("testMethod", response);

        assertThat(exception).isInstanceOf(DomainException.class);

        DomainException domainException = (DomainException) exception;
        assertThat(domainException.getCode()).isEqualTo(ExceptionCode.BAD_REQUEST_ERROR);
    }

    @Test
    void should_decode_401_unauthorized() {
        assertStatus(401, ExceptionCode.UNAUTHORIZED_ERROR);
    }

    @Test
    void should_decode_403_forbidden() {
        assertStatus(403, ExceptionCode.FORBIDDEN_ERROR);
    }

    @Test
    void should_decode_404_not_found() {
        assertStatus(404, ExceptionCode.NOT_FOUND_ERROR);
    }

    @Test
    void should_decode_408_timeout() {
        assertStatus(408, ExceptionCode.REQUEST_TIMEOUT_ERROR);
    }

    @Test
    void should_decode_429_too_many_requests() {
        assertStatus(429, ExceptionCode.TOO_MANY_REQUESTS_ERROR);
    }

    @Test
    void should_decode_500_internal_error() {
        assertStatus(500, ExceptionCode.INTERNAL_ERROR);
    }

    @Test
    void should_decode_502_bad_gateway() {
        assertStatus(502, ExceptionCode.BAD_GATEWAY_ERROR);
    }

    @Test
    void should_decode_503_service_unavailable() {
        assertStatus(503, ExceptionCode.SERVICE_UNAVAILABLE_ERROR);
    }

    @Test
    void should_decode_504_gateway_timeout() {
        assertStatus(504, ExceptionCode.GATEWAY_TIMEOUT_ERROR);
    }

    @Test
    void should_decode_default_external_service_error() {
        assertStatus(999, ExceptionCode.EXTERNAL_SERVICE_ERROR);
    }

    @Test
    void should_handle_null_body() {
        Response response = Response.builder()
                .status(500)
                .reason("Internal Server Error")
                .request(dummyRequest())
                .headers(Collections.emptyMap())
                .build();

        Exception exception = decoder.decode("testMethod", response);

        assertThat(exception).isInstanceOf(DomainException.class);
    }

    @Test
    void should_handle_io_exception_when_reading_body() {
        Response.Body faultyBody = new Response.Body() {
            @Override
            public Integer length() {
                return null;
            }

            @Override
            public boolean isRepeatable() {
                return false;
            }

            @Override
            public java.io.InputStream asInputStream() throws IOException {
                throw new IOException("boom");
            }

            @Override
            public java.io.Reader asReader() {
                return null;
            }

            @Override
            public java.io.Reader asReader(java.nio.charset.Charset charset) {
                return null;
            }

            @Override
            public void close() {
            }
        };

        Response response = Response.builder()
                .status(500)
                .reason("Internal Server Error")
                .request(dummyRequest())
                .headers(Collections.emptyMap())
                .body(faultyBody)
                .build();

        Exception exception = decoder.decode("testMethod", response);

        assertThat(exception).isInstanceOf(DomainException.class);
    }

    private void assertStatus(int status, String expectedCode) {
        Response response = buildResponse(status, "error body");

        Exception exception = decoder.decode("testMethod", response);

        assertThat(exception).isInstanceOf(DomainException.class);

        DomainException domainException = (DomainException) exception;
        assertThat(domainException.getCode()).isEqualTo(expectedCode);
    }

    private Response buildResponse(int status, String body) {
        return Response.builder()
                .status(status)
                .reason("error")
                .request(dummyRequest())
                .headers(Collections.emptyMap())
                .body(body, StandardCharsets.UTF_8)
                .build();
    }


    private Request dummyRequest() {
        return Request.create(
                Request.HttpMethod.GET,
                "http://localhost/test",
                Collections.emptyMap(),
                null,
                StandardCharsets.UTF_8,
                null
        );
    }
}
