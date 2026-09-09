package io.qoop.stream.publisher;

import io.qoop.stream.api.EventPublisher;
import io.qoop.stream.api.Header;
import io.qoop.stream.api.annotaions.Event;
import lombok.RequiredArgsConstructor;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

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
        List<Header> headers = resolveHeaders(payload);
        sendRecord(channel, null, key, payload, headers);
    }

    @Override
    public void publish(Object payload) {
        String channel = resolveChannel(payload);
        List<Header> headers = resolveHeaders(payload);
        sendRecord(channel, null, null, payload, headers);
    }

    @Override
    public void publish(List<Header> headers, String key, Object payload) {
        String channel = resolveChannel(payload);
        List<Header> mergedHeaders = mergeHeaders(resolveHeaders(payload), headers);
        sendRecord(channel, null, key, payload, mergedHeaders);
    }

    @Override
    public void publish(String topic, List<Header> headers, String key, Object payload) {
        List<Header> mergedHeaders = mergeHeaders(resolveHeaders(payload), headers);
        sendRecord(topic, null, key, payload, mergedHeaders);
    }

    @Override
    public void publish(String topic, List<Header> headers, Object payload) {
        List<Header> mergedHeaders = mergeHeaders(resolveHeaders(payload), headers);
        sendRecord(topic, null, null, payload, mergedHeaders);
    }

    @Override
    public void publish(String topic, String key, Object payload) {
        List<Header> headers = resolveHeaders(payload);
        sendRecord(topic, null, key, payload, headers);
    }

    @Override
    public void publishWithTopic(String topic, Object payload) {
        List<Header> headers = resolveHeaders(payload);
        sendRecord(topic, null, null, payload, headers);
    }

    @Override
    public void publish(List<Header> headers, Object payload) {
        String channel = resolveChannel(payload);
        List<Header> mergedHeaders = mergeHeaders(resolveHeaders(payload), headers);
        sendRecord(channel, null, null, payload, mergedHeaders);
    }

    /**
     * Resolve the Kafka topic/channel from the @Event annotation on the payload class.
     * If annotation is missing, fallback to lowercase class name.
     */
    private String resolveChannel(Object payload) {
        Event annotation = payload.getClass().getAnnotation(Event.class);
        if (annotation != null && !annotation.value().isEmpty()) {
            return annotation.value();
        }
        return payload.getClass().getSimpleName().toLowerCase();
    }

    /**
     * Extract headers defined within the @Event annotation if present.
     */
    private List<Header> resolveHeaders(Object payload) {
        List<Header> extractedHeaders = new ArrayList<>();
        Event annotation = payload.getClass().getAnnotation(Event.class);
        if (annotation != null && annotation.headers().length > 0) {
            extractedHeaders.addAll(
                    Arrays.stream(annotation.headers())
                            .map(header -> new Header(header.name(), header.value()))
                            .collect(Collectors.toSet())
            );
        }

        return extractedHeaders;
    }

    /**
     * Merge annotation-level headers with dynamically passed headers.
     */
    private List<Header> mergeHeaders(List<Header> baseHeaders, List<Header> additionalHeaders) {
        List<Header> result = new ArrayList<>(baseHeaders);
        if (additionalHeaders != null) {
            result.addAll(additionalHeaders);
        }
        return result;
    }

    /**
     * Helper method to construct and send a ProducerRecord via KafkaTemplate.
     */
    private void sendRecord(String topic, Integer partition, String key, Object payload, List<Header> headers) {
        ProducerRecord<String, Object> record = new ProducerRecord<>(topic, partition, key, payload);
        if (headers != null) {
            for (Header header : headers) {
                record.headers().add(header.getName(), header.getValue().getBytes(StandardCharsets.UTF_8));
            }
        }

        kafkaTemplate.send(record);
    }
}
