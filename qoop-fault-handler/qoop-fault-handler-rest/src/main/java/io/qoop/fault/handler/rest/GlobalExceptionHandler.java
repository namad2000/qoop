package io.qoop.fault.handler.rest;


import io.qoop.fault.handler.api.exception.DomainException;
import io.qoop.fault.handler.api.exception.DomainValidationException;
import io.qoop.fault.handler.rest.dto.ErrorResponse;
import io.qoop.fault.handler.rest.dto.FieldErrorDetail;
import io.qoop.message.api.MessageResolver;
import jakarta.validation.ConstraintViolationException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Locale;

import static io.qoop.fault.handler.api.exception.ExceptionCode.*;
import static io.qoop.logs.LogKeys.MDC_KEY;

@Slf4j
@RestControllerAdvice
@RequiredArgsConstructor
public class GlobalExceptionHandler {

    @Value("${error.internal.message.show:true}")
    private boolean showMessage;

    private final MessageResolver messageResolver;


    // Handle MethodArgumentNotValidException (Hibernate Validator on @RequestBody)
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleMethodArgumentNotValidException(
            MethodArgumentNotValidException ex,
            Locale locale
    ) {
        log.error("Validation failed for request body", ex);

        List<
                FieldErrorDetail> fieldErrors = ex.getBindingResult()
                .getFieldErrors()
                .stream()
                .map(fieldError -> {
                    String field = fieldError.getField();

                    return new FieldErrorDetail(
                            messageResolver.resolveField(field, locale),
                            field,
                            messageResolver.resolve(fieldError.getDefaultMessage(), locale)
                    );
                })
                .toList();

        ErrorResponse response = ErrorResponse.builder()
                .timestamp(LocalDateTime.now())
                .correlationId(MDC.get(MDC_KEY))
                .code(VALIDATION_FAILED)
                .message(messageResolver.resolve(VALIDATION_FAILED, locale))
                .fieldErrors(fieldErrors)
                .build();

        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(response);
    }

    // Handle ConstraintViolationException (Hibernate Validator on Params / PathVariables)
    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ErrorResponse> handleConstraintViolationException(
            ConstraintViolationException ex,
            Locale locale
    ) {
        log.error("Constraint violation occurred", ex);

        List<FieldErrorDetail> fieldErrors = ex.getConstraintViolations()
                .stream()
                .map(violation -> {
                    String field = violation.getPropertyPath().toString();

                    return new FieldErrorDetail(
                            messageResolver.resolveField(field, locale),
                            field,
                            messageResolver.resolve(violation.getMessage(), locale)
                    );
                })
                .toList();

        ErrorResponse response = ErrorResponse.builder()
                .timestamp(LocalDateTime.now())
                .correlationId(MDC.get(MDC_KEY))
                .code(CONSTRAINT_VIOLATION)
                .message(messageResolver.resolve(CONSTRAINT_VIOLATION, locale))
                .fieldErrors(fieldErrors)
                .build();

        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(response);
    }

    // Handle DomainValidationException
    @ExceptionHandler(DomainValidationException.class)
    public ResponseEntity<ErrorResponse> handleDomainValidationException(
            DomainValidationException ex,
            Locale locale
    ) {
        log.error("An unexpected error occurred while processing the request", ex);

        ErrorResponse response = ErrorResponse.builder()
                .timestamp(LocalDateTime.now())
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
                .timestamp(LocalDateTime.now())
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
                .timestamp(LocalDateTime.now())
                .correlationId(MDC.get(MDC_KEY))
                .code(INTERNAL_ERROR)
                .message(message)
                .build();

        return ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(response);
    }
}