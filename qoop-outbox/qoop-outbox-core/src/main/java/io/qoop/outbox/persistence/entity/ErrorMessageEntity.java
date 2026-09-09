package io.qoop.outbox.persistence.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

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
    @Column(name = "id", nullable = false, updatable = false)
    private UUID id = UUID.randomUUID();

    @Column(name = "topic", length = 200)
    private String topic;

    @Column(name = "message_key")
    private String key;

    @Lob
    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "payload", columnDefinition = "json")
    private String payload;

    @Column(name = "error_message", length = 2000)
    private String errorMessage;

    @Column(name = "exception_class", length = 500)
    private String exceptionClass;

    @Lob
    @Column(name = "stack_trace", columnDefinition = "TEXT")
    private String stackTrace;

    @Column(name = "correlation_id", length = 100)
    private String correlationId;

    @Column(name = "timestamp")
    private Instant timestamp = Instant.now();
}