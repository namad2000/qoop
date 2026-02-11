package io.qoop.fault.handler.rest;

import io.qoop.fault.handler.api.dto.ErrorResponse;
import io.qoop.fault.handler.api.exception.DomainException;
import io.qoop.fault.handler.api.exception.DomainValidationException;
import io.qoop.message.api.MessageResolver;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.Locale;

import static io.qoop.fault.handler.api.exception.ExceptionCode.INTERNAL_ERROR;
import static io.qoop.logs.LogKeys.MDC_KEY;

@Slf4j
@RestControllerAdvice
@RequiredArgsConstructor
public class GlobalExceptionHandler {

    @Value("${error.internal.message.show:true}")
    private boolean showMessage;

    private final MessageResolver messageResolver;

    // Handle DomainValidationException
    @ExceptionHandler(DomainValidationException.class)
    public ResponseEntity<ErrorResponse> handleDomainValidationException(
            DomainValidationException ex,
            Locale locale
    ) {
        log.error("An unexpected error occurred while processing the request", ex);

        ErrorResponse response = ErrorResponse.builder()
                .correlationId(MDC.get(MDC_KEY))
                .code(ex.getCode())
                .message(messageResolver.resolve(ex.getCode(), locale, ex.getParams()))
                .field(ex.getParamName())
                .build();

        return ResponseEntity
                .status(ex.getHttpStatus())
                .body(response);
    }

    // Handle general DomainException
    @ExceptionHandler(DomainException.class)
    public ResponseEntity<ErrorResponse> handleDomainException(
            DomainException ex,
            Locale locale
    ) {
        log.error("An unexpected error occurred while processing the request", ex);

        ErrorResponse response = ErrorResponse.builder()
                .correlationId(MDC.get(MDC_KEY))
                .code(ex.getCode())
                .message(messageResolver.resolve(ex.getCode(), locale, ex.getParams()))
                .build();

        return ResponseEntity
                .status(ex.getHttpStatus())
                .body(response);
    }

    // Handle Generic Exception
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGenericException(
            Exception ex,
            Locale locale
    ) {
        log.error("An unexpected error occurred while processing the request", ex);

        String message = showMessage
                ? ex.getMessage()
                : messageResolver.resolve(INTERNAL_ERROR, locale);

        ErrorResponse response = ErrorResponse.builder()
                .correlationId(MDC.get(MDC_KEY))
                .code(INTERNAL_ERROR)
                .message(message)
                .build();

        return ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(response);
    }
}
