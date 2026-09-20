package io.qoop.stream.subscriber.config;

import io.qoop.stream.api.ErrorMessage;
import io.qoop.stream.publisher.config.KafkaPublisherConfig;
import io.qoop.stream.starter.KafkaProperties;
import org.junit.jupiter.api.Test;
import org.slf4j.MDC;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.test.context.EmbeddedKafka;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ContextConfiguration;

import java.util.concurrent.TimeUnit;

import static io.qoop.logs.LogKeys.MDC_KEY;
import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;

@SpringBootTest(
        properties = {
                "event.stream.bootstrap-servers=${spring.embedded.kafka.brokers}",
                "event.stream.dlq.enabled=true",
                "event.stream.dlq.suffix=DLT",
                "event.stream.dlq.global-dlq.enabled=false",
                "event.stream.retry.max-attempts=2",
                "event.stream.retry.backoff-ms=100",
                "logging.level.org.springframework.kafka=DEBUG",
                "logging.level.io.qoop.stream=DEBUG"
        }
)
@EnableConfigurationProperties(KafkaProperties.class)
@DirtiesContext
@EmbeddedKafka(partitions = 1, topics = {"test-topic", "test-topic.DLT"})
@ContextConfiguration(classes = {
        KafkaPublisherConfig.class,
        KafkaSubscriberConfig.class,
        EventSubscriberConfig.class,
        TestConsumer.class,
        TestDlqConsumer.class
})
class KafkaIntegrationLocalDlqTest {

    @Autowired
    private KafkaTemplate<String, Object> kafkaTemplate;

    @Autowired
    private TestDlqConsumer testDlqConsumer;

    @Test
    void shouldRouteFailedMessageToLocalDlq() {
        String expectedCorrelationId = "test-correlation-local-123";
        MDC.put(MDC_KEY, expectedCorrelationId);

        String testMessage = "hello-world-local";

        kafkaTemplate.executeInTransaction(template -> {
            template.send("test-topic", testMessage);
            return null;
        });

        await().atMost(30, TimeUnit.SECONDS)
                .until(() -> !testDlqConsumer.dlqQueue.isEmpty());

        ErrorMessage error = testDlqConsumer.dlqQueue.poll();
        assertThat(error).isNotNull();
        assertThat(error.getPayload()).isEqualTo(testMessage);
        assertThat(error.getErrorMessage()).contains("fail processing");
        assertThat(error.getExceptionClass()).contains("RuntimeException");
        assertThat(error.getTopic()).isEqualTo("test-topic");
        assertThat(error.getTimestamp()).isNotNull();
        assertThat(error.getCorrelationId()).isEqualTo(expectedCorrelationId);
    }
}