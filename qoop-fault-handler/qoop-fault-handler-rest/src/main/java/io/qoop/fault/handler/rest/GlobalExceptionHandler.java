package io.qoop.fault.handler.rest;

import io.qoop.fault.handler.api.exception.DomainException;
import io.qoop.fault.handler.api.exception.DomainValidationException;
import io.qoop.fault.handler.rest.dto.ErrorResponse;
import io.qoop.fault.handler.rest.dto.FieldErrorDetail;
import io.qoop.logs.DomainLogger;
import io.qoop.message.api.MessageResolver;
import jakarta.validation.ConstraintViolationException;
import lombok.RequiredArgsConstructor;
import org.slf4j.MDC;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.resource.NoResourceFoundException;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Locale;

import static io.qoop.fault.handler.api.exception.ExceptionCode.*;
import static io.qoop.logs.LogKeys.MDC_KEY;

@RestControllerAdvice
@RequiredArgsConstructor
public class GlobalExceptionHandler {

    public static final String LOG_KEY_VALIDATION = "FAULT_HANDLE_VALIDATION";
    public static final String LOG_KEY_SECURITY = "FAULT_HANDLE_SECURITY";
    public static final String LOG_KEY_DOMAIN = "FAULT_HANDLE_DOMAIN";
    public static final String LOG_KEY_HTTP = "FAULT_HANDLE_HTTP";
    public static final String LOG_KEY_GENERIC = "FAULT_HANDLE_GENERIC";

    @Value("${error.internal.message.show:true}")
    private boolean showMessage;

    private final MessageResolver messageResolver;
    private final DomainLogger logger;

    // =========================================================================
    // Security Exceptions (401 & 403)
    // =========================================================================

    @ExceptionHandler(AuthenticationException.class)
    public ResponseEntity<ErrorResponse> handleAuthenticationException(
            AuthenticationException ex,
            Locale locale
    ) {
        logger.logErrorForClass(
                GlobalExceptionHandler.class,
                LOG_KEY_SECURITY,
                "Authentication failed: {}",
                ex.getMessage()
        );

        ErrorResponse response = ErrorResponse.builder()
                .timestamp(LocalDateTime.now())
                .correlationId(MDC.get(MDC_KEY))
                .code(UNAUTHORIZED_ERROR)
                .message(messageResolver.resolve(UNAUTHORIZED_ERROR, locale))
                .build();

        return ResponseEntity
                .status(HttpStatus.UNAUTHORIZED)
                .body(response);
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ErrorResponse> handleAccessDeniedException(
            AccessDeniedException ex,
            Locale locale
    ) {
        logger.logErrorForClass(
                GlobalExceptionHandler.class,
                LOG_KEY_SECURITY,
                "Access denied for current user: {}",
                ex.getMessage()
        );

        ErrorResponse response = ErrorResponse.builder()
                .timestamp(LocalDateTime.now())
                .correlationId(MDC.get(MDC_KEY))
                .code(FORBIDDEN_ERROR)
                .message(messageResolver.resolve(FORBIDDEN_ERROR, locale))
                .build();

        return ResponseEntity
                .status(HttpStatus.FORBIDDEN)
                .body(response);
    }

    // =========================================================================
    // Validation Exceptions (400)
    // =========================================================================

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleMethodArgumentNotValidException(
            MethodArgumentNotValidException ex,
            Locale locale
    ) {
        logger.logErrorForClass(
                GlobalExceptionHandler.class,
                LOG_KEY_VALIDATION,
                "Validation failed for request body. Field count: {}",
                ex.getBindingResult().getFieldErrorCount()
        );

        List<FieldErrorDetail> fieldErrors = ex.getBindingResult()
                .getFieldErrors()
                .stream()
                .map(fieldError -> new FieldErrorDetail(
                        messageResolver.resolveField(fieldError.getField(), locale),
                        fieldError.getField(),
                        messageResolver.resolve(fieldError.getDefaultMessage(), locale)
                ))
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

    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ErrorResponse> handleConstraintViolationException(
            ConstraintViolationException ex,
            Locale locale
    ) {
        logger.logErrorForClass(
                GlobalExceptionHandler.class,
                LOG_KEY_VALIDATION,
                "Constraint violation occurred: {}",
                ex.getMessage()
        );

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

    // =========================================================================
    // Domain & Business Exceptions
    // =========================================================================

    @ExceptionHandler(DomainValidationException.class)
    public ResponseEntity<ErrorResponse> handleDomainValidationException(
            DomainValidationException ex,
            Locale locale
    ) {
        logger.logErrorForClass(
                GlobalExceptionHandler.class,
                LOG_KEY_DOMAIN,
                "Domain validation error occurred. Code: {}, Field: {}",
                ex.getCode(),
                ex.getParamName()
        );

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

    @ExceptionHandler(DomainException.class)
    public ResponseEntity<ErrorResponse> handleDomainException(
            DomainException ex,
            Locale locale
    ) {
        logger.logErrorForClass(
                GlobalExceptionHandler.class,
                LOG_KEY_DOMAIN,
                "Domain error occurred. Code: {}, Status: {}",
                ex.getCode(),
                ex.getHttpStatus()
        );

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

    // =========================================================================
    // Common Spring REST Exceptions (400, 404, 405)
    // =========================================================================

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ErrorResponse> handleHttpMessageNotReadableException(
            HttpMessageNotReadableException ex,
            Locale locale
    ) {
        logger.logErrorForClass(
                GlobalExceptionHandler.class,
                LOG_KEY_HTTP,
                "Malformed JSON request body: {}",
                ex.getMessage()
        );

        ErrorResponse response = ErrorResponse.builder()
                .timestamp(LocalDateTime.now())
                .correlationId(MDC.get(MDC_KEY))
                .code(BAD_REQUEST_ERROR)
                .message(messageResolver.resolve(BAD_REQUEST_ERROR, locale))
                .build();

        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(response);
    }

    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    public ResponseEntity<ErrorResponse> handleMethodNotSupportedException(
            HttpRequestMethodNotSupportedException ex,
            Locale locale
    ) {
        logger.logErrorForClass(
                GlobalExceptionHandler.class,
                LOG_KEY_HTTP,
                "HTTP method not supported: {}",
                ex.getMethod()
        );

        ErrorResponse response = ErrorResponse.builder()
                .timestamp(LocalDateTime.now())
                .correlationId(MDC.get(MDC_KEY))
                .code(BAD_REQUEST_ERROR)
                .message(messageResolver.resolve(BAD_REQUEST_ERROR, locale))
                .build();

        return ResponseEntity
                .status(HttpStatus.METHOD_NOT_ALLOWED)
                .body(response);
    }

    @ExceptionHandler(NoResourceFoundException.class)
    public ResponseEntity<ErrorResponse> handleNoResourceFoundException(
            NoResourceFoundException ex,
            Locale locale
    ) {
        logger.logErrorForClass(
                GlobalExceptionHandler.class,
                LOG_KEY_HTTP,
                "Requested resource not found: {}",
                ex.getResourcePath()
        );

        ErrorResponse response = ErrorResponse.builder()
                .timestamp(LocalDateTime.now())
                .correlationId(MDC.get(MDC_KEY))
                .code(NOT_FOUND_ERROR)
                .message(messageResolver.resolve(NOT_FOUND_ERROR, locale))
                .build();

        return ResponseEntity
                .status(HttpStatus.NOT_FOUND)
                .body(response);
    }

    // =========================================================================
    // Generic Fallback Exception (500)
    // =========================================================================

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGenericException(
            Exception ex,
            Locale locale
    ) {
        logger.logErrorForClass(
                GlobalExceptionHandler.class,
                LOG_KEY_GENERIC,
                "An unexpected system exception occurred: {}",
                ex.getMessage()
        );

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