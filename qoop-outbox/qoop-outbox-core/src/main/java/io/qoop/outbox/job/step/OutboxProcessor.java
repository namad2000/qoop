package io.qoop.outbox.job.step;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.qoop.outbox.kafka.KafkaMessage;
import io.qoop.outbox.persistence.entity.OutboxEventEntity;
import io.qoop.stream.api.Header;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.infrastructure.item.ItemProcessor;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * ItemProcessor for converting OutboxEventEntity to KafkaMessage.
 *
 * <p>Transforms the database entity into a message suitable for Kafka publishing.
 * Headers are parsed from JSON and preserved in the message.</p>
 */
@Slf4j
@Component
@StepScope
@RequiredArgsConstructor
public class OutboxProcessor implements ItemProcessor<OutboxEventEntity, KafkaMessage> {

    private final ObjectMapper objectMapper;

    @Override
    public KafkaMessage process(OutboxEventEntity event) throws Exception {
        List<Header> headers = null;

        if (event.getHeaders() != null && !event.getHeaders().isBlank()) {
            try {
                headers = objectMapper.readValue(
                        event.getHeaders(),
                        new TypeReference<>() {
                        }
                );
            } catch (Exception e) {
                log.error("Failed to parse outbox event headers for event: {}", event.getId(), e);
                throw new RuntimeException("Failed to parse outbox event headers", e);
            }
        }

        return new KafkaMessage(
                event.getId(),
                event.getAggregateId(),
                event.getTopic(),
                headers,
                event.getPayload(),
                event.getVersion()
        );
    }
}