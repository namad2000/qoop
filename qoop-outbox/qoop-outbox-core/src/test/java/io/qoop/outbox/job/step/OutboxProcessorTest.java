package io.qoop.outbox.job.step;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.qoop.outbox.kafka.KafkaMessage;
import io.qoop.outbox.persistence.entity.OutboxEventEntity;
import io.qoop.stream.api.Header;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("Outbox Processor Tests")
class OutboxProcessorTest {

    @Mock
    private ObjectMapper objectMapper;

    private OutboxProcessor processor;

    @BeforeEach
    void setUp() {
        processor = new OutboxProcessor(objectMapper);
    }

    @Test
    @DisplayName("Should process event without headers")
    void shouldProcessEventWithoutHeaders() throws Exception {
        // Given
        OutboxEventEntity event = new OutboxEventEntity();
        event.setId(UUID.randomUUID());
        event.setAggregateId("agg-123");
        event.setTopic("test-topic");
        event.setPayload("{\"key\":\"value\"}");
        event.setVersion(0L);
        event.setHeaders(null);

        // When
        KafkaMessage result = processor.process(event);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.eventId()).isEqualTo(event.getId());
        assertThat(result.aggregateId()).isEqualTo("agg-123");
        assertThat(result.topic()).isEqualTo("test-topic");
        assertThat(result.value()).isEqualTo("{\"key\":\"value\"}");
        assertThat(result.version()).isEqualTo(0L);
        assertThat(result.headers()).isNull();
    }

    @Test
    @DisplayName("Should process event with valid headers")
    void shouldProcessEventWithValidHeaders() throws Exception {
        // Given
        String headersJson = "[{\"name\":\"traceId\",\"value\":\"123\"}]";
        List<Header> expectedHeaders = List.of(new Header("traceId", "123"));

        OutboxEventEntity event = new OutboxEventEntity();
        event.setId(UUID.randomUUID());
        event.setAggregateId("agg-123");
        event.setTopic("test-topic");
        event.setPayload("{\"key\":\"value\"}");
        event.setVersion(0L);
        event.setHeaders(headersJson);

        when(objectMapper.readValue(any(String.class), any(TypeReference.class)))
                .thenReturn(expectedHeaders);

        // When
        KafkaMessage result = processor.process(event);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.headers()).isEqualTo(expectedHeaders);
    }

    @Test
    @DisplayName("Should throw exception when headers parsing fails")
    void shouldThrowExceptionWhenHeadersParsingFails() throws Exception {
        // Given
        OutboxEventEntity event = new OutboxEventEntity();
        event.setId(UUID.randomUUID());
        event.setHeaders("invalid-json");
        event.setAggregateId("agg-123");
        event.setTopic("test-topic");
        event.setPayload("{\"key\":\"value\"}");
        event.setVersion(0L);

        when(objectMapper.readValue(any(String.class), any(TypeReference.class)))
                .thenThrow(new RuntimeException("Parse error"));

        // When & Then
        assertThatThrownBy(() -> processor.process(event))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Failed to parse outbox event headers");
    }
}