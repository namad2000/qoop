package io.qoop.outbox.job.step;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.qoop.outbox.kafka.KafkaMessage;
import io.qoop.outbox.mapper.ErrorMessageMapper;
import io.qoop.outbox.persistence.entity.ErrorMessageEntity;
import io.qoop.outbox.persistence.repository.OutboxEventJpaRepository;
import io.qoop.outbox.service.OutboxErrorLogService;
import io.qoop.stream.api.EventPublisher;
import io.qoop.stream.api.Header;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.batch.infrastructure.item.Chunk;
import org.springframework.batch.infrastructure.item.ItemWriter;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

/**
 * ItemWriter for publishing Kafka messages and updating outbox status.
 *
 * <p>Publishes messages to Kafka and updates the entity status to SENT.
 * Uses optimistic locking to prevent duplicate processing across nodes.</p>
 *
 * <p>On failure, the entity is marked as FAILED and error details are persisted.</p>
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class OutboxWriter implements ItemWriter<KafkaMessage> {

    private final EventPublisher publisher;
    private final OutboxEventJpaRepository repository;
    private final OutboxErrorLogService outboxErrorLogService;
    private final ErrorMessageMapper errorMessageMapper;
    private final ObjectMapper objectMapper;

    @Override
    public void write(Chunk<? extends KafkaMessage> chunk) throws Exception {
        for (KafkaMessage message : chunk.getItems()) {
            final UUID eventId = message.eventId();
            final String topic = message.topic();
            final List<Header> headers = message.headers();
            final Object value = message.value();
            final Long version = message.version();
            final String correlationId = MDC.get("correlationId");

            try {
                // Publish message to Kafka
                publisher.publish(topic, headers, value);

                // Mark as sent with version verification
                int updatedRows = repository.markAsSent(eventId, LocalDateTime.now(), version);
                if (updatedRows == 0) {
                    throw new OptimisticLockingFailureException(
                            "Optimistic lock failure: Event already processed by another worker for ID: " + eventId
                    );
                }

                log.debug("Successfully published and marked as sent: eventId={}", eventId);

            } catch (Exception ex) {
                // Log error and mark as failed
                ErrorMessageEntity errorMessage = errorMessageMapper.toEntity(message, ex, correlationId, objectMapper);

                outboxErrorLogService.handleFailure(eventId, version, errorMessage);

                log.error("Failed to publish outbox event: eventId={}", eventId, ex);
                throw new RuntimeException("Failed to publish outbox event for ID: " + eventId, ex);
            }
        }
    }
}