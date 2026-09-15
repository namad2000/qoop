package io.qoop.outbox.job.step;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.qoop.outbox.OutboxStatus;
import io.qoop.outbox.kafka.KafkaMessage;
import io.qoop.outbox.mapper.ErrorMessageMapper;
import io.qoop.outbox.persistence.entity.ErrorMessageEntity;
import io.qoop.outbox.persistence.entity.OutboxEventEntity;
import io.qoop.outbox.persistence.repository.OutboxEventJpaRepository;
import io.qoop.outbox.service.OutboxErrorLogService;
import io.qoop.stream.api.EventPublisher;
import io.qoop.stream.api.Header;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.infrastructure.item.Chunk;
import org.springframework.batch.infrastructure.item.ItemWriter;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Slf4j
@Component
@StepScope
@RequiredArgsConstructor
public class OutboxWriter implements ItemWriter<KafkaMessage> {

    private final EventPublisher publisher;
    private final OutboxEventJpaRepository repository;
    private final OutboxErrorLogService outboxErrorLogService;
    private final ErrorMessageMapper errorMessageMapper;
    private final ObjectMapper objectMapper;

    @Override
    @Transactional
    public void write(Chunk<? extends KafkaMessage> chunk) {
        for (KafkaMessage message : chunk.getItems()) {
            final UUID eventId = message.eventId();
            final String topic = message.topic();
            final List<Header> headers = message.headers();
            final Object value = message.value();
            final Long expectedVersion = message.version();
            final String correlationId = MDC.get("correlationId");

            try {
                publisher.publish(topic, headers, value);

                OutboxEventEntity entity = repository.findById(eventId)
                        .orElseThrow(() -> new EntityNotFoundException("Event not found with ID: " + eventId));

                entity.setStatus(OutboxStatus.SENT);
                entity.setSentAt(LocalDateTime.now());

                repository.save(entity);

                log.debug("Successfully published and marked as sent: eventId={}", eventId);

            } catch (ObjectOptimisticLockingFailureException ex) {
                log.error("Optimistic lock failure for eventId={}. Modified by another transaction.", eventId, ex);
                throw ex;

            } catch (Exception ex) {
                log.error("Failed to publish outbox event: eventId={}", eventId, ex);

                try {
                    ErrorMessageEntity errorMessage = errorMessageMapper.toEntity(message, ex, correlationId, objectMapper);
                    outboxErrorLogService.handleFailure(eventId, expectedVersion, errorMessage);
                } catch (Exception logEx) {
                    log.error("Failed to log error details for eventId={}", eventId, logEx);
                }

                throw new RuntimeException("Failed to publish outbox event for ID: " + eventId, ex);
            }
        }
    }
}