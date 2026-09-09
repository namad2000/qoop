package io.qoop.stream.publisher;

import io.qoop.stream.api.Header;
import io.qoop.stream.api.annotaions.Event;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.kafka.core.KafkaTemplate;

import java.nio.charset.StandardCharsets;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.Mockito.*;

class KafkaEventPublisherTest {

    private KafkaTemplate<String, Object> kafkaTemplate;
    private KafkaEventPublisher publisher;

    @BeforeEach
    void setUp() {
        kafkaTemplate = mock(KafkaTemplate.class);
        publisher = new KafkaEventPublisher(kafkaTemplate);
    }

    // Sample Event with annotation and annotation-level headers
    @Event(value = "annotated-channel", headers = {@io.qoop.stream.api.annotaions.Header(name = "env", value = "prod")})
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

        ArgumentCaptor<ProducerRecord<String, Object>> recordCaptor = ArgumentCaptor.forClass(ProducerRecord.class);
        verify(kafkaTemplate, times(1)).send(recordCaptor.capture());

        ProducerRecord<String, Object> capturedRecord = recordCaptor.getValue();
        assertEquals("annotated-channel", capturedRecord.topic());
        assertEquals(key, capturedRecord.key());
        assertEquals(event, capturedRecord.value());
        assertEquals("prod", new String(capturedRecord.headers().lastHeader("env").value(), StandardCharsets.UTF_8));
    }

    @Test
    void testPublishWithoutKeyUsesAnnotation() {
        AnnotatedEvent event = new AnnotatedEvent();

        publisher.publish(event);

        ArgumentCaptor<ProducerRecord<String, Object>> recordCaptor = ArgumentCaptor.forClass(ProducerRecord.class);
        verify(kafkaTemplate, times(1)).send(recordCaptor.capture());

        ProducerRecord<String, Object> capturedRecord = recordCaptor.getValue();
        assertEquals("annotated-channel", capturedRecord.topic());
        assertEquals(event, capturedRecord.value());
    }

    @Test
    void testPublishFallbackToClassName() {
        NonAnnotatedEvent event = new NonAnnotatedEvent();
        String key = "key1";

        publisher.publish(key, event);

        ArgumentCaptor<ProducerRecord<String, Object>> recordCaptor = ArgumentCaptor.forClass(ProducerRecord.class);
        verify(kafkaTemplate, times(1)).send(recordCaptor.capture());

        ProducerRecord<String, Object> capturedRecord = recordCaptor.getValue();
        assertEquals("nonannotatedevent", capturedRecord.topic());
        assertEquals(key, capturedRecord.key());
        assertEquals(event, capturedRecord.value());
    }

    @Test
    void testPublishWithHeadersKeyAndPayload() {
        AnnotatedEvent event = new AnnotatedEvent();
        String key = "key-headers";
        List<Header> dynamicHeaders = List.of(new Header("traceId", "12345"));

        publisher.publish(dynamicHeaders, key, event);

        ArgumentCaptor<ProducerRecord<String, Object>> recordCaptor = ArgumentCaptor.forClass(ProducerRecord.class);
        verify(kafkaTemplate, times(1)).send(recordCaptor.capture());

        ProducerRecord<String, Object> capturedRecord = recordCaptor.getValue();
        assertEquals("annotated-channel", capturedRecord.topic());
        assertEquals(key, capturedRecord.key());
        assertEquals(event, capturedRecord.value());
        assertEquals("prod", new String(capturedRecord.headers().headers("env").iterator().next().value(), StandardCharsets.UTF_8));
        assertEquals("12345", new String(capturedRecord.headers().headers("traceId").iterator().next().value(), StandardCharsets.UTF_8));
    }

    @Test
    void testPublishWithTopicHeadersKeyAndPayload() {
        AnnotatedEvent event = new AnnotatedEvent();
        String explicitTopic = "custom-topic";
        String key = "custom-key";
        List<Header> dynamicHeaders = List.of(new Header("version", "v1"));

        publisher.publish(explicitTopic, dynamicHeaders, key, event);

        ArgumentCaptor<ProducerRecord<String, Object>> recordCaptor = ArgumentCaptor.forClass(ProducerRecord.class);
        verify(kafkaTemplate, times(1)).send(recordCaptor.capture());

        ProducerRecord<String, Object> capturedRecord = recordCaptor.getValue();
        assertEquals(explicitTopic, capturedRecord.topic());
        assertEquals(key, capturedRecord.key());
        assertEquals(event, capturedRecord.value());
        assertEquals("v1", new String(capturedRecord.headers().headers("version").iterator().next().value(), StandardCharsets.UTF_8));
    }

    @Test
    void testPublishWithTopicHeadersAndPayload() {
        AnnotatedEvent event = new AnnotatedEvent();
        String explicitTopic = "custom-topic-no-key";
        List<Header> dynamicHeaders = List.of(new Header("region", "us-east"));

        publisher.publish(explicitTopic, dynamicHeaders, event);

        ArgumentCaptor<ProducerRecord<String, Object>> recordCaptor = ArgumentCaptor.forClass(ProducerRecord.class);
        verify(kafkaTemplate, times(1)).send(recordCaptor.capture());

        ProducerRecord<String, Object> capturedRecord = recordCaptor.getValue();
        assertEquals(explicitTopic, capturedRecord.topic());
        assertNull(capturedRecord.key());
        assertEquals(event, capturedRecord.value());
        assertEquals("us-east", new String(capturedRecord.headers().headers("region").iterator().next().value(), StandardCharsets.UTF_8));
    }

    @Test
    void testPublishWithTopicAndKey() {
        NonAnnotatedEvent event = new NonAnnotatedEvent();
        String explicitTopic = "explicit-topic";
        String key = "key-only";

        publisher.publish(explicitTopic, key, event);

        ArgumentCaptor<ProducerRecord<String, Object>> recordCaptor = ArgumentCaptor.forClass(ProducerRecord.class);
        verify(kafkaTemplate, times(1)).send(recordCaptor.capture());

        ProducerRecord<String, Object> capturedRecord = recordCaptor.getValue();
        assertEquals(explicitTopic, capturedRecord.topic());
        assertEquals(key, capturedRecord.key());
        assertEquals(event, capturedRecord.value());
    }

    @Test
    void testPublishWithTopicOnly() {
        NonAnnotatedEvent event = new NonAnnotatedEvent();
        String explicitTopic = "explicit-topic-only";

        publisher.publishWithTopic(explicitTopic, event);

        ArgumentCaptor<ProducerRecord<String, Object>> recordCaptor = ArgumentCaptor.forClass(ProducerRecord.class);
        verify(kafkaTemplate, times(1)).send(recordCaptor.capture());

        ProducerRecord<String, Object> capturedRecord = recordCaptor.getValue();
        assertEquals(explicitTopic, capturedRecord.topic());
        assertEquals(event, capturedRecord.value());
    }

    @Test
    void testPublishWithHeadersAndPayload() {
        AnnotatedEvent event = new AnnotatedEvent();
        List<Header> dynamicHeaders = List.of(new Header("auth", "bearer"));

        publisher.publish(dynamicHeaders, event);

        ArgumentCaptor<ProducerRecord<String, Object>> recordCaptor = ArgumentCaptor.forClass(ProducerRecord.class);
        verify(kafkaTemplate, times(1)).send(recordCaptor.capture());

        ProducerRecord<String, Object> capturedRecord = recordCaptor.getValue();
        assertEquals("annotated-channel", capturedRecord.topic());
        assertEquals(event, capturedRecord.value());
        assertEquals("bearer", new String(capturedRecord.headers().headers("auth").iterator().next().value(), StandardCharsets.UTF_8));
    }
}