package ir.online.commons.stream.starter;

import lombok.Data;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.apache.kafka.common.serialization.StringSerializer;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.support.serializer.JacksonJsonDeserializer;
import org.springframework.kafka.support.serializer.JacksonJsonSerializer;

import java.util.HashMap;
import java.util.Map;

@Data
@Configuration
@ConfigurationProperties(prefix = "event.stream")
public class KafkaProperties {

    // ===========================
    // Core
    // ===========================
    private String bootstrapServers = "localhost:9092";

    private Producer producer = new Producer();
    private Consumer consumer = new Consumer();
    private Retry retry = new Retry();
    private Dlq dlq = new Dlq();
    private Transaction transaction = new Transaction();
    private Security security = new Security();

    // ===========================
    // Producer Config
    // ===========================
    @Data
    public static class Producer {
        private String acks = "all";
        private int retries = Integer.MAX_VALUE;
        private boolean enableIdempotence = true;
        private int batchSize = 16384;
        private int lingerMs = 5;
        private long bufferMemory = 33554432;
        private String compressionType = "lz4";

        private Map<String, Object> properties = new HashMap<>();
    }

    // ===========================
    // Consumer Config
    // ===========================
    @Data
    public static class Consumer {
        private String groupId = "event-group";
        private String autoOffsetReset = "earliest";
        private boolean enableAutoCommit = false; // MUST false for EOS
        private int concurrency = 3;
        private int maxPollRecords = 500;
        private int sessionTimeoutMs = 10000;
        private int maxPollIntervalMs = 300000;

        private Map<String, Object> properties = new HashMap<>();
    }

    // ===========================
    // Retry Config
    // ===========================
    @Data
    public static class Retry {
        private long maxAttempts = 3;
        private long backoffMs = 1000;
    }

    // ===========================
    // DLQ Config
    // ===========================
    @Data
    public static class Dlq {
        private boolean enabled = true;

        private String suffix = "DLT";

        // General DLQ topic name
        private GlobalDlq globalDlq = new GlobalDlq();
    }

    // ===========================
    // DLQ Config
    // ===========================
    @Data
    public static class GlobalDlq {
        private boolean enabled = true;

        // General DLQ topic name
        private String topic = "global-dlt-topic";
    }

    // ===========================
    // Transaction Config (EOS)
    // ===========================
    @Data
    public static class Transaction {
        private boolean enabled = true;
        private String transactionalIdPrefix = "event-tx-";
    }

    // ===========================
    // Security Config
    // ===========================
    @Data
    public static class Security {
        private String trustedPackages = "*";
    }

    // ===========================
    // Producer Properties Builder
    // ===========================
    public Map<String, Object> producerProps() {
        Map<String, Object> props = new HashMap<>();

        props.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        props.put(ProducerConfig.ACKS_CONFIG, producer.getAcks());
        props.put(ProducerConfig.RETRIES_CONFIG, producer.getRetries());
        props.put(ProducerConfig.ENABLE_IDEMPOTENCE_CONFIG, producer.isEnableIdempotence());
        props.put(ProducerConfig.BATCH_SIZE_CONFIG, producer.getBatchSize());
        props.put(ProducerConfig.LINGER_MS_CONFIG, producer.getLingerMs());
        props.put(ProducerConfig.BUFFER_MEMORY_CONFIG, producer.getBufferMemory());
        props.put(ProducerConfig.COMPRESSION_TYPE_CONFIG, producer.getCompressionType());

        props.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
        props.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, JacksonJsonSerializer.class);

        // Transactional Producer (Exactly Once)
        if (transaction.isEnabled()) {
            props.put(ProducerConfig.TRANSACTIONAL_ID_CONFIG, transaction.getTransactionalIdPrefix());
        }

        props.putAll(producer.getProperties());
        return props;
    }

    // ===========================
    // Consumer Properties Builder
    // ===========================
    public Map<String, Object> consumerProps() {
        Map<String, Object> props = new HashMap<>();

        props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        props.put(ConsumerConfig.GROUP_ID_CONFIG, consumer.getGroupId());
        props.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, consumer.getAutoOffsetReset());
        props.put(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, consumer.isEnableAutoCommit());
        props.put(ConsumerConfig.MAX_POLL_RECORDS_CONFIG, consumer.getMaxPollRecords());
        props.put(ConsumerConfig.SESSION_TIMEOUT_MS_CONFIG, consumer.getSessionTimeoutMs());
        props.put(ConsumerConfig.MAX_POLL_INTERVAL_MS_CONFIG, consumer.getMaxPollIntervalMs());

        props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
        props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, JacksonJsonDeserializer.class);

        props.put("spring.json.trusted.packages", security.getTrustedPackages());

        props.putAll(consumer.getProperties());
        return props;
    }
}

