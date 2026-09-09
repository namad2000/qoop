package io.qoop.outbox.kafka;

import io.qoop.stream.api.Header;

import java.util.List;
import java.util.UUID;

public record KafkaMessage(
        UUID eventId,
        String aggregateId,
        String topic,
        List<Header> headers, // Parsed metadata headers
        Object value,         // Message payload content
        Long version          // Entity version for Optimistic Locking validation
) {
}