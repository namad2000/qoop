package io.qoop.feign;

import feign.Response;
import feign.codec.ErrorDecoder;
import io.qoop.fault.handler.api.exception.DomainException;
import io.qoop.fault.handler.api.exception.ExceptionCode;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.StreamUtils;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

@Slf4j
public class QoopErrorDecoder implements ErrorDecoder {

    @Override
    public Exception decode(String methodKey, Response response) {
        int status = response.status();
        String responseBody = "";

        try {
            if (response.body() != null) {
                responseBody = StreamUtils.copyToString(response.body().asInputStream(), StandardCharsets.UTF_8);
            }
        } catch (IOException e) {
            log.error("QOOP-FEIGN | Error reading body | Method: {} | Error: {}", methodKey, e.getMessage());
        }

        log.error("QOOP-FEIGN | Call Failed | Method: {} | Status: {} | Response: {}", methodKey, status, responseBody);

        String errorCode = switch (status) {
            case 400 -> ExceptionCode.BAD_REQUEST_ERROR;
            case 401 -> ExceptionCode.UNAUTHORIZED_ERROR;
            case 403 -> ExceptionCode.FORBIDDEN_ERROR;
            case 404 -> ExceptionCode.NOT_FOUND_ERROR;
            case 408 -> ExceptionCode.REQUEST_TIMEOUT_ERROR;
            case 429 -> ExceptionCode.TOO_MANY_REQUESTS_ERROR;
            case 500 -> ExceptionCode.INTERNAL_ERROR;
            case 502 -> ExceptionCode.BAD_GATEWAY_ERROR;
            case 503 -> ExceptionCode.SERVICE_UNAVAILABLE_ERROR;
            case 504 -> ExceptionCode.GATEWAY_TIMEOUT_ERROR;
            default -> ExceptionCode.EXTERNAL_SERVICE_ERROR;
        };

        return DomainException.of(errorCode, status, methodKey, responseBody);
    }
}