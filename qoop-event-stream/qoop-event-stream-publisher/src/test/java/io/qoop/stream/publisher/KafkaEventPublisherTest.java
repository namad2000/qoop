package io.qoop.stream.publisher;

import io.qoop.stream.api.Event;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.kafka.core.KafkaTemplate;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

class KafkaEventPublisherTest {

    private KafkaTemplate<String, Object> kafkaTemplate;
    private KafkaEventPublisher publisher;

    @BeforeEach
    void setUp() {
        kafkaTemplate = mock(KafkaTemplate.class);
        publisher = new KafkaEventPublisher(kafkaTemplate);
    }

    // Sample Event with annotation
    @Event("annotated-channel")
    static class AnnotatedEvent {
        private final String data = "test";
    }

    // Sample Event without annotation
    static class NonAnnotatedEvent {
        private final String data = "test";
    }

    @Test
    void testPublishWithKeyUsesAnnotation() {
        AnnotatedEvent event = new AnnotatedEvent();
        String key = "my-key";

        publisher.publish(key, event);

        ArgumentCaptor<String> topicCaptor = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<String> keyCaptor = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<Object> payloadCaptor = ArgumentCaptor.forClass(Object.class);

        // Verify that KafkaTemplate.send() was called with the correct topic, key, and payload
        verify(kafkaTemplate, times(1))
                .send(topicCaptor.capture(), keyCaptor.capture(), payloadCaptor.capture());

        assertEquals("annotated-channel", topicCaptor.getValue());
        assertEquals(key, keyCaptor.getValue());
        assertEquals(event, payloadCaptor.getValue());
    }

    @Test
    void testPublishWithoutKeyUsesAnnotation() {
        AnnotatedEvent event = new AnnotatedEvent();

        publisher.publish(event);

        ArgumentCaptor<String> topicCaptor = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<Object> payloadCaptor = ArgumentCaptor.forClass(Object.class);

        // Verify that KafkaTemplate.send() was called with the correct topic and payload (no key)
        verify(kafkaTemplate, times(1))
                .send(topicCaptor.capture(), payloadCaptor.capture());

        assertEquals("annotated-channel", topicCaptor.getValue());
        assertEquals(event, payloadCaptor.getValue());
    }

    @Test
    void testPublishFallbackToClassName() {
        NonAnnotatedEvent event = new NonAnnotatedEvent();
        String key = "key1";

        publisher.publish(key, event);

        ArgumentCaptor<String> topicCaptor = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<String> keyCaptor = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<Object> payloadCaptor = ArgumentCaptor.forClass(Object.class);

        // Verify that KafkaTemplate.send() was called with the fallback topic (class name lowercase)
        verify(kafkaTemplate, times(1))
                .send(topicCaptor.capture(), keyCaptor.capture(), payloadCaptor.capture());

        assertEquals("nonannotatedevent", topicCaptor.getValue()); // fallback to class name
        assertEquals(key, keyCaptor.getValue());
        assertEquals(event, payloadCaptor.getValue());
    }
}
