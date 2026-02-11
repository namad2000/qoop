package ir.online.commons.stream.publisher.config;

import ir.online.commons.stream.starter.KafkaProperties;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.kafka.autoconfigure.KafkaAutoConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.core.DefaultKafkaProducerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.core.ProducerFactory;
import org.springframework.kafka.transaction.KafkaTransactionManager;

import java.util.Map;

@Configuration
@RequiredArgsConstructor
@EnableAutoConfiguration(exclude = {KafkaAutoConfiguration.class})
public class KafkaPublisherConfig {

    private final KafkaProperties kafkaProperties;

    @Bean
    public ProducerFactory<String, Object> producerFactory() {
        Map<String, Object> configProps = kafkaProperties.producerProps();

        // Register interceptor for Correlation ID
        configProps.put(org.apache.kafka.clients.producer.ProducerConfig.INTERCEPTOR_CLASSES_CONFIG,
                CorrelationIdKafkaInterceptor.class.getName());

        return getKafkaProducerFactory(configProps);
    }

    @Bean
    public KafkaTemplate<String, Object> kafkaTemplate(ProducerFactory<String, Object> producerFactory) {
        KafkaTemplate<String, Object> template = new KafkaTemplate<>(producerFactory);
        // Note: No need to set prefix here as it's handled in the factory.
        return template;
    }

    // Required for managing transactions on the consumer side
    @Bean
    public KafkaTransactionManager transactionManager(ProducerFactory<String, Object> producerFactory) {
        if (kafkaProperties.getTransaction().isEnabled()) {
            return new KafkaTransactionManager<>(producerFactory);
        }
        return null;
    }

    private DefaultKafkaProducerFactory<String, Object> getKafkaProducerFactory(Map<String, Object> configProps) {
        DefaultKafkaProducerFactory<String, Object> factory =
                new DefaultKafkaProducerFactory<>(configProps);

        if (kafkaProperties.getTransaction().isEnabled()) {
            // Set transaction prefix. Ensure uniqueness in clustered environments.
            String prefix = kafkaProperties.getTransaction().getTransactionalIdPrefix();
            factory.setTransactionIdPrefix(prefix);

            // Enable transaction capability
            factory.transactionCapable();
        }
        return factory;
    }
}