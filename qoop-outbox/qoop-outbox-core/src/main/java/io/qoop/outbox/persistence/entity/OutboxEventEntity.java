package io.qoop.outbox.persistence.entity;

import io.qoop.outbox.OutboxStatus;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Getter
@Setter
@Table(name = "outbox_event")
public class OutboxEventEntity {

    @Id
    @Column(name = "id", nullable = false, updatable = false, columnDefinition = "RAW(16)")
    private UUID id = UUID.randomUUID();

    @Column(name = "aggregatetype", nullable = false, length = 100)
    private String aggregateType; // Type of the aggregate or entity generating the event (e.g., Order)

    @Column(name = "aggregateid", nullable = false, length = 100)
    private String aggregateId; // Unique identifier or key of that specific entity (e.g., the order ID)

    @Column(name = "type", nullable = false, length = 500)
    private String eventType; // Type of event or name of the action performed (e.g., OrderCreated)

    @Column(name = "topic", nullable = false, length = 200)
    private String topic; // Destination Kafka topic where the message should be sent (single topic per record for Debezium compatibility)

    @Column(name = "payload", nullable = false, columnDefinition = "CLOB")
    private String payload = "{}";

    @Column(name = "headers", columnDefinition = "CLOB")
    private String headers = "[]";

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private OutboxStatus status = OutboxStatus.NEW; // Current status of the message (e.g., NEW, SENT, FAILED)

    @Column(name = "retry_count", nullable = false)
    private int retryCount = 0; // Number of retry attempts for sending the message in case of failure

    @Version
    @Column(name = "version", nullable = false)
    private Long version = 0L; // Version field for Hibernate Optimistic Locking

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt = LocalDateTime.now(); // Timestamp when the event was initially created

    @Column(name = "sent_at")
    private LocalDateTime sentAt; // Exact timestamp when the message was successfully sent to Kafka
}