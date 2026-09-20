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
                "event.stream.dlq.global-dlq.enabled=true",
                "event.stream.dlq.global-dlq.topic=global-dlt-topic",
                "event.stream.retry.max-attempts=2",
                "event.stream.retry.backoff-ms=100",
                "logging.level.org.springframework.kafka=DEBUG",
                "logging.level.io.qoop.stream=DEBUG"
        }
)
@EnableConfigurationProperties(KafkaProperties.class)
@DirtiesContext
@EmbeddedKafka(partitions = 1, topics = {"test-topic", "global-dlt-topic"})
@ContextConfiguration(classes = {
        KafkaPublisherConfig.class,
        KafkaSubscriberConfig.class,
        EventSubscriberConfig.class,
        TestConsumer.class,
        TestGlobalDlq.class
})
class KafkaIntegrationGlobalDlqTest {

    @Autowired
    private KafkaTemplate<String, Object> kafkaTemplate;

    @Autowired
    private TestGlobalDlq testGlobalDlq;

    @Test
    void shouldRouteFailedMessageToGlobalDlq() {
        String expectedCorrelationId = "test-correlation-global-456";
        MDC.put(MDC_KEY, expectedCorrelationId);

        String testMessage = "hello-world-global";

        kafkaTemplate.executeInTransaction(template -> {
            template.send("test-topic", testMessage);
            return null;
        });

        await().atMost(30, TimeUnit.SECONDS)
                .until(() -> !testGlobalDlq.dlqQueue.isEmpty());

        ErrorMessage error = testGlobalDlq.dlqQueue.poll();
        assertThat(error).isNotNull();
        assertThat(error.getPayload()).isEqualTo(testMessage);
        assertThat(error.getErrorMessage()).contains("fail processing");
        assertThat(error.getExceptionClass()).contains("RuntimeException");
        assertThat(error.getTopic()).isEqualTo("test-topic");
        assertThat(error.getTimestamp()).isNotNull();
        assertThat(error.getCorrelationId()).isEqualTo(expectedCorrelationId);
    }
}