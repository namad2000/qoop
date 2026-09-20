package io.qoop.stream.subscriber.config;

import io.qoop.stream.api.ErrorMessage;
import io.qoop.stream.publisher.config.KafkaPublisherConfig;
import io.qoop.stream.starter.KafkaProperties;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.listener.ConcurrentMessageListenerContainer;
import org.springframework.kafka.listener.DefaultErrorHandler;
import org.springframework.kafka.listener.MessageListener;
import org.springframework.kafka.test.EmbeddedKafkaBroker;
import org.springframework.kafka.test.context.EmbeddedKafka;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ContextConfiguration;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

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
@EmbeddedKafka(
        partitions = 1,
        topics = {
                "test-topic",
                "test-topic.DLT"
        }
)
@ContextConfiguration(classes = {
        KafkaPublisherConfig.class,
        KafkaSubscriberConfig.class,
        EventSubscriberConfig.class,
})
public class KafkaIntegrationContainerTest {

    @Autowired
    private KafkaTemplate<String, Object> kafkaTemplate;

    @Autowired
    private KafkaProperties kafkaProperties;

    @Autowired
    private KafkaSubscriberConfig kafkaSubscriberConfig;

    @Autowired
    private DefaultErrorHandler defaultErrorHandler;

    @Autowired
    private EmbeddedKafkaBroker embeddedKafkaBroker;

    private ConcurrentMessageListenerContainer<String, Object> mainContainer;
    private ConcurrentMessageListenerContainer<String, Object> dlqContainer;

    @AfterEach
    void tearDown() {
        // Stop containers after each test execution to free up resources
        if (mainContainer != null) mainContainer.stop();
        if (dlqContainer != null) dlqContainer.stop();
    }

    @Test
    void testRetryAndDlqWithConfiguredConsumer() {

        BlockingQueue<ErrorMessage> dlqMessages = new LinkedBlockingQueue<>();

        // === Setup Main Consumer programmatically ===
        mainContainer = kafkaSubscriberConfig.kafkaListenerContainerFactory(defaultErrorHandler)
                .createContainer("test-topic");

        mainContainer.setupMessageListener((MessageListener<String, Object>) record -> {
            throw new RuntimeException("fail processing");
        });
        mainContainer.start();

        // === Setup DLQ Consumer programmatically ===
        dlqContainer = kafkaSubscriberConfig.kafkaListenerContainerFactory(defaultErrorHandler)
                .createContainer("test-topic." + kafkaProperties.getDlq().getSuffix());

        dlqContainer.setupMessageListener((MessageListener<String, Object>) record -> {
            dlqMessages.add((ErrorMessage) record.value());
        });
        dlqContainer.start();

        // === Send message to main topic inside a transaction ===
        kafkaTemplate.executeInTransaction(template -> {
            template.send("test-topic", "hello-world");
            return null;
        });

        // === Wait until DLQ receives the error message ===
        await().atMost(15, TimeUnit.SECONDS)
                .until(() -> !dlqMessages.isEmpty());

        // === Assertions and verifications ===
        ErrorMessage errorMessage = dlqMessages.poll();

        assertThat(errorMessage).isNotNull();

        // Validate original payload
        assertThat(errorMessage.getPayload()).isEqualTo("hello-world");

        // Validate error details
        assertThat(errorMessage.getErrorMessage()).contains("fail processing");
        assertThat(errorMessage.getExceptionClass()).contains("RuntimeException");
        assertThat(errorMessage.getStackTrace()).contains("RuntimeException");

        // Validate metadata
        assertThat(errorMessage.getTopic()).isEqualTo("test-topic");
        assertThat(errorMessage.getTimestamp()).isNotNull();
    }
}