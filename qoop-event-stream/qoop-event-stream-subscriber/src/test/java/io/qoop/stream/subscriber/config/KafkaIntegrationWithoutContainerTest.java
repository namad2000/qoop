package ir.online.commons.stream.subscriber.config;

import ir.online.commons.stream.api.ErrorMessage;
import ir.online.commons.stream.publisher.config.KafkaPublisherConfig;
import ir.online.commons.stream.starter.KafkaProperties;
import org.junit.jupiter.api.Test;
import org.slf4j.MDC;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.test.context.EmbeddedKafka;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ContextConfiguration;

import java.util.concurrent.TimeUnit;

import static io.qoop.logs.LogKeys.MDC_KEY;
import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;
import static org.awaitility.Awaitility.await;

@SpringBootTest(
        properties = {
                "event.stream.bootstrap-servers=${spring.embedded.kafka.brokers}",
                "event.stream.dlq.global-dlq.enabled=false"
        }
)
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
        KafkaProperties.class,
        EventSubscriberConfig.class,
        TestConsumer.class,      // Adding the main consumer
        TestDlqConsumer.class    // Adding the DLQ consumer
})
public class KafkaIntegrationWithoutContainerTest {

    @Autowired
    private KafkaTemplate<String, Object> kafkaTemplate;

    @Autowired
    private TestDlqConsumer testDlqConsumer; // Injected to access the DLQ queue

    @Autowired
    private TestConsumer testConsumer;       // Injected to access the main consumer state

    @Test
    void testRetryAndDlqWithConfiguredConsumer() {

        String expectedCorrelationId = "test-correlation-123";
        MDC.put(MDC_KEY, expectedCorrelationId);

        String testMessage = "hello-world";

        kafkaTemplate.executeInTransaction(template -> {
            template.send("test-topic", testMessage);
            return null;
        });

        await().atMost(15, TimeUnit.SECONDS)
                .until(() -> !testDlqConsumer.dlqQueue.isEmpty() || !testConsumer.mainQueue.isEmpty());

        if (!testDlqConsumer.dlqQueue.isEmpty()) {

            ErrorMessage error = testDlqConsumer.dlqQueue.poll();

            assertThat(error).isNotNull();
            assertThat(error.getPayload()).isEqualTo(testMessage);
            assertThat(error.getErrorMessage()).contains("fail processing");
            assertThat(error.getExceptionClass()).contains("RuntimeException");
            assertThat(error.getTopic()).isEqualTo("test-topic");
            assertThat(error.getTimestamp()).isNotNull();
            assertThat(error.getCorrelationId()).isEqualTo(expectedCorrelationId);

            System.out.println("✅ Test Passed: Message successfully sent to DLQ.");

        } else if (!testConsumer.mainQueue.isEmpty()) {
            throw new AssertionError("Message processed successfully. Retry config is wrong.");
        }
    }
}