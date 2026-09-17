package io.qoop.batch.config;

import io.qoop.properties.factory.YamlPropertySourceFactory;
import io.qoop.stream.starter.KafkaProperties;
import lombok.RequiredArgsConstructor;
import org.springframework.batch.core.step.StepExecution;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import org.springframework.integration.channel.DirectChannel;
import org.springframework.integration.channel.QueueChannel;
import org.springframework.integration.config.EnableIntegration;
import org.springframework.integration.dsl.IntegrationFlow;
import org.springframework.integration.kafka.dsl.Kafka;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.listener.ContainerProperties;

import java.util.Collections;

@Configuration
@EnableIntegration
@RequiredArgsConstructor
@PropertySource(value = "classpath:qoop-batch.yml", factory = YamlPropertySourceFactory.class)
public class BatchKafkaIntegrationConfig {

    private final KafkaProperties kafkaProperties;
    private final BatchKafkaProperties batchKafkaProperties;
    private final KafkaTemplate<String, Object> kafkaTemplate;
    private final ConsumerFactory<String, Object> consumerFactory;
    private final BatchPayloadConverter payloadConverter;

    private String getRequestTopic() {
        return batchKafkaProperties.getKafka().getResolvedRequestTopic();
    }

    private String getReplyTopic() {
        return batchKafkaProperties.getKafka().getResolvedReplyTopic();
    }

    private String resolveGroupId() {
        String baseGroup = (kafkaProperties.getConsumer() != null && kafkaProperties.getConsumer().getGroupId() != null)
                ? kafkaProperties.getConsumer().getGroupId()
                : "qoop-batch-group";
        return batchKafkaProperties.getKafka().getResolvedGroupId(baseGroup);
    }

    @Bean(name = "${batch.kafka.namespace:default}-outboundRequests")
    public DirectChannel outboundRequests() {
        return new DirectChannel();
    }

    @Bean(name = "${batch.kafka.namespace:default}-inboundReplies")
    public QueueChannel inboundReplies() {
        return new QueueChannel();
    }

    @Bean(name = "${batch.kafka.namespace:default}-inboundRequests")
    public DirectChannel inboundRequests() {
        return new DirectChannel();
    }

    @Bean(name = "${batch.kafka.namespace:default}-outboundReplies")
    public DirectChannel outboundReplies() {
        return new DirectChannel();
    }

    @Bean
    public IntegrationFlow outboundRequestsFlow() {
        return IntegrationFlow.from(outboundRequests())
                .transform(payloadConverter::convertToMapIfNeeded)
                .handle(Kafka.outboundChannelAdapter(kafkaTemplate)
                        .topic(getRequestTopic()))
                .get();
    }

    @Bean
    public IntegrationFlow inboundRepliesFlow() {
        ContainerProperties containerProperties = new ContainerProperties(getReplyTopic());
        containerProperties.setGroupId(resolveGroupId() + "-master-replies");

        return IntegrationFlow.from(
                        Kafka.messageDrivenChannelAdapter(consumerFactory, containerProperties))
                .transform(payload -> {
                    Object converted = payloadConverter.convertToStepExecutionIfNeeded(payload);

                    if (converted instanceof StepExecution stepExec) {
                        return Collections.singletonList(stepExec);
                    } else if (converted instanceof java.util.Collection<?> collection) {
                        return collection;
                    } else {
                        return Collections.singletonList(converted);
                    }
                })
                .channel(inboundReplies())
                .get();
    }

    @Bean
    public IntegrationFlow inboundRequestsFlow() {
        ContainerProperties containerProperties = new ContainerProperties(getRequestTopic());
        containerProperties.setGroupId(resolveGroupId());

        return IntegrationFlow.from(
                        Kafka.messageDrivenChannelAdapter(consumerFactory, containerProperties))
                .channel(inboundRequests())
                .get();
    }

    @Bean
    public IntegrationFlow outboundRepliesFlow() {
        return IntegrationFlow.from(outboundReplies())
                .transform(payloadConverter::convertToMapIfNeeded)
                .handle(Kafka.outboundChannelAdapter(kafkaTemplate)
                        .topic(getReplyTopic()))
                .get();
    }
}