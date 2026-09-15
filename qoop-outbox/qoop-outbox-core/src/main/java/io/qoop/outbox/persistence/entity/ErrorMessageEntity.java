package io.qoop.outbox.persistence.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;
import java.util.UUID;

@Getter
@Setter
@Entity
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "outbox_error_message")
public class ErrorMessageEntity {

    @Id
    @Column(name = "id", nullable = false, updatable = false, columnDefinition = "RAW(16)")
    private UUID id = UUID.randomUUID();

    @Column(name = "topic", length = 200)
    private String topic;

    @Column(name = "key", length = 200)
    private String key;

    @Column(name = "payload", columnDefinition = "CLOB")
    private String payload = "{}";

    @Column(name = "error_message", length = 2000)
    private String errorMessage;

    @Column(name = "exception_class", length = 500)
    private String exceptionClass;

    @Column(name = "stack_trace", columnDefinition = "CLOB")
    private String stackTrace;

    @Column(name = "correlation_id", length = 100)
    private String correlationId;

    @Column(name = "timestamp")
    private Instant timestamp = Instant.now();
}