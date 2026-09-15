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
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.slf4j.MDC;
import org.springframework.batch.infrastructure.item.Chunk;
import org.springframework.orm.ObjectOptimisticLockingFailureException;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("Outbox Writer Tests")
class OutboxWriterTest {

    @Mock
    private EventPublisher publisher;

    @Mock
    private OutboxEventJpaRepository repository;

    @Mock
    private OutboxErrorLogService outboxErrorLogService;

    @Mock
    private ErrorMessageMapper errorMessageMapper;

    @Mock
    private ObjectMapper objectMapper;

    private OutboxWriter writer;

    @BeforeEach
    void setUp() {
        writer = new OutboxWriter(publisher, repository, outboxErrorLogService, errorMessageMapper, objectMapper);
        MDC.put("correlationId", "corr-123");
    }

    @Test
    @DisplayName("Should publish message and mark as sent successfully")
    void shouldPublishMessageAndMarkAsSentSuccessfully() throws Exception {
        // Given
        UUID eventId = UUID.randomUUID();
        KafkaMessage message = createKafkaMessage(eventId, "test-topic", 0L);
        Chunk<KafkaMessage> chunk = new Chunk<>(List.of(message));

        OutboxEventEntity entity = new OutboxEventEntity();
        entity.setId(eventId);
        entity.setStatus(OutboxStatus.NEW);

        when(repository.findById(eventId)).thenReturn(Optional.of(entity));
        when(repository.save(any(OutboxEventEntity.class))).thenAnswer(invocation -> invocation.getArgument(0));
        doNothing().when(publisher).publish(eq("test-topic"), (List<Header>) isNull(), any());

        // When
        writer.write(chunk);

        // Then
        verify(publisher, times(1)).publish(eq("test-topic"), (List<Header>) isNull(), any());
        verify(repository, times(1)).findById(eventId);
        verify(repository, times(1)).save(entity);
        assertThat(entity.getStatus()).isEqualTo(OutboxStatus.SENT);
        assertThat(entity.getSentAt()).isNotNull();
        verify(outboxErrorLogService, never()).handleFailure(any(), any(), any());
    }

    @Test
    @DisplayName("Should handle optimistic lock failure")
    void shouldHandleOptimisticLockFailure() throws Exception {
        // Given
        UUID eventId = UUID.randomUUID();
        KafkaMessage message = createKafkaMessage(eventId, "test-topic", 0L);
        Chunk<KafkaMessage> chunk = new Chunk<>(List.of(message));

        OutboxEventEntity entity = new OutboxEventEntity();
        entity.setId(eventId);

        doNothing().when(publisher).publish(eq("test-topic"), (List<Header>) isNull(), any());
        when(repository.findById(eventId)).thenReturn(Optional.of(entity));

        doThrow(new ObjectOptimisticLockingFailureException(OutboxEventEntity.class, eventId))
                .when(repository).save(any(OutboxEventEntity.class));

        // When & Then
        assertThatThrownBy(() -> writer.write(chunk))
                .isInstanceOf(ObjectOptimisticLockingFailureException.class);

        verify(publisher, times(1)).publish(eq("test-topic"), (List<Header>) isNull(), any());
        verify(repository, times(1)).findById(eventId);
        verify(repository, times(1)).save(any());
        verify(outboxErrorLogService, never()).handleFailure(any(), any(), any());
    }

    @Test
    @DisplayName("Should handle publisher exception")
    void shouldHandlePublisherException() throws Exception {
        // Given
        UUID eventId = UUID.randomUUID();
        KafkaMessage message = createKafkaMessage(eventId, "test-topic", 0L);
        Chunk<KafkaMessage> chunk = new Chunk<>(List.of(message));

        doThrow(new RuntimeException("Kafka error")).when(publisher).publish(eq("test-topic"), (List<Header>) isNull(), any());
        when(errorMessageMapper.toEntity(any(), any(), any(), any())).thenReturn(new ErrorMessageEntity());

        // When & Then
        assertThatThrownBy(() -> writer.write(chunk))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Failed to publish outbox event for ID: " + eventId);

        verify(publisher, times(1)).publish(eq("test-topic"), (List<Header>) isNull(), any());
        verify(repository, never()).findById(any());
        verify(repository, never()).save(any());
        verify(outboxErrorLogService, times(1)).handleFailure(eq(eventId), eq(0L), any());
    }

    @Test
    @DisplayName("Should process multiple messages in chunk")
    void shouldProcessMultipleMessagesInChunk() throws Exception {
        // Given
        UUID eventId1 = UUID.randomUUID();
        UUID eventId2 = UUID.randomUUID();
        KafkaMessage msg1 = createKafkaMessage(eventId1, "topic-1", 0L);
        KafkaMessage msg2 = createKafkaMessage(eventId2, "topic-2", 1L);
        Chunk<KafkaMessage> chunk = new Chunk<>(List.of(msg1, msg2));

        OutboxEventEntity entity1 = new OutboxEventEntity();
        entity1.setId(eventId1);
        OutboxEventEntity entity2 = new OutboxEventEntity();
        entity2.setId(eventId2);

        doNothing().when(publisher).publish(anyString(), (List<Header>) isNull(), any());
        when(repository.findById(eventId1)).thenReturn(Optional.of(entity1));
        when(repository.findById(eventId2)).thenReturn(Optional.of(entity2));
        when(repository.save(any(OutboxEventEntity.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // When
        writer.write(chunk);

        // Then
        verify(publisher, times(2)).publish(anyString(), (List<Header>) isNull(), any());
        verify(repository, times(1)).findById(eventId1);
        verify(repository, times(1)).findById(eventId2);
        verify(repository, times(2)).save(any());
    }

    private KafkaMessage createKafkaMessage(UUID eventId, String topic, Long version) {
        return new KafkaMessage(
                eventId,
                "agg-123",
                topic,
                null,
                "{\"key\":\"value\"}",
                version
        );
    }
}