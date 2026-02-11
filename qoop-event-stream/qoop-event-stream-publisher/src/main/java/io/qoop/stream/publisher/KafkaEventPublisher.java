package ir.online.commons.stream.publisher;

import ir.online.commons.stream.api.Event;
import ir.online.commons.stream.api.EventPublisher;
import lombok.RequiredArgsConstructor;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

/**
 * Kafka-based implementation of EventPublisher.
 * Determines the channel from the @Event annotation on the payload class.
 */
@Service
@RequiredArgsConstructor
public class KafkaEventPublisher implements EventPublisher {

    private final KafkaTemplate<String, Object> kafkaTemplate;

    @Override
    public void publish(String key, Object payload) {
        String channel = resolveChannel(payload);
        kafkaTemplate.send(channel, key, payload);
    }

    @Override
    public void publish(Object payload) {
        String channel = resolveChannel(payload);
        kafkaTemplate.send(channel, payload);
    }

    /**
     * Resolve the Kafka topic/channel from the @Event annotation on the payload class.
     * If annotation is missing, fallback to lowercase class name.
     */
    private String resolveChannel(Object payload) {
        Event annotation = payload.getClass().getAnnotation(Event.class);
        if (annotation != null) {
            return annotation.value();
        }
        return payload.getClass().getSimpleName().toLowerCase();
    }
}
