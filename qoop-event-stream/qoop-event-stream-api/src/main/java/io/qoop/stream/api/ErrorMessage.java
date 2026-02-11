package ir.online.commons.stream.api;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import java.time.Instant;

@Data
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
public class ErrorMessage {

    private String topic;
    private String key;

    private Object payload;          // Original Kafka message

    private String errorMessage;     // Root cause message
    private String exceptionClass;   // Exception class name
    private String stackTrace;        // Full stacktrace

    private String correlationId;    // MDC Correlation ID
    private Instant timestamp;        // Error time
}
