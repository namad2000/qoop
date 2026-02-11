package io.qoop.stream.subscriber.config;

import io.qoop.stream.api.ErrorMessage;
import io.qoop.stream.starter.KafkaProperties;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.slf4j.MDC;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.annotation.EnableKafka;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.listener.ConsumerRecordRecoverer;
import org.springframework.kafka.listener.DefaultErrorHandler;
import org.springframework.util.backoff.FixedBackOff;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Optional;

import static io.qoop.logs.LogKeys.CORRELATION_ID_HEADER;
import static io.qoop.logs.LogKeys.MDC_KEY;

@EnableKafka
@Configuration
@RequiredArgsConstructor
@Slf4j
public class EventSubscriberConfig {

    private final KafkaProperties kafkaProperties;

    @Bean
    public DefaultErrorHandler errorHandler(KafkaTemplate<String, Object> template) {

        // Custom recoverer to handle errors and send to DLQ
        ConsumerRecordRecoverer recoverer = (record, ex) -> {
            if (!kafkaProperties.getDlq().isEnabled()) {
                log.warn("DLQ is disabled. Message will not be sent. Key={}, Topic={}",
                        record.key(), record.topic());
                return;
            }

            String targetTopic = resolveTargetTopic(record);
            Throwable root = getRootCause(ex);
            String correlationId = extractCorrelationId(record);

            // Set MDC for logging
            MDC.put(MDC_KEY, correlationId);

            ErrorMessage errorMessage = ErrorMessage.builder()
                    .topic(record.topic())
                    .key((String) record.key())
                    .payload(record.value())
                    .errorMessage(root.getMessage())
                    .exceptionClass(root.getClass().getName())
                    .stackTrace(getStackTraceAsString(ex))
                    .correlationId(correlationId)
                    .timestamp(Instant.now())
                    .build();

            log.error("DLQ PUBLISHING: targetTopic={}, originalTopic={}, key={}, correlationId={}, exception={}",
                    targetTopic, record.topic(), record.key(), correlationId, root.getClass().getName());

            // Send to DLQ.
            // executeInTransaction ensures a new transaction is used if transactions are enabled,
            // preventing the DLQ send from failing due to the main transaction rollback.
            template.executeInTransaction(t -> t.send(targetTopic, (String) record.key(), errorMessage));
        };

        // Configure retry policy
        long maxAttempts = kafkaProperties.getRetry().getMaxAttempts();
        FixedBackOff backOff = new FixedBackOff(
                kafkaProperties.getRetry().getBackoffMs(),
                maxAttempts > 1 ? maxAttempts - 1 : 0
        );

        return new DefaultErrorHandler(recoverer, backOff);
    }

    private String resolveTargetTopic(ConsumerRecord<?, ?> record) {
        if (kafkaProperties.getDlq().getGlobalDlq() != null
                && kafkaProperties.getDlq().getGlobalDlq().isEnabled()) {
            return kafkaProperties.getDlq().getGlobalDlq().getTopic();
        }
        return record.topic() + "." + kafkaProperties.getDlq().getSuffix();
    }

    private String extractCorrelationId(ConsumerRecord<?, ?> record) {
        return Optional.ofNullable(record.headers().lastHeader(CORRELATION_ID_HEADER))
                .map(h -> new String(h.value(), StandardCharsets.UTF_8))
                .orElse("UNKNOWN"); // Return UNKNOWN instead of random UUID to preserve traceability
    }

    private String getStackTraceAsString(Throwable throwable) {
        StringWriter sw = new StringWriter();
        throwable.printStackTrace(new PrintWriter(sw));
        return sw.toString();
    }

    private Throwable getRootCause(Throwable ex) {
        Throwable cause = ex;
        while (cause.getCause() != null) {
            cause = cause.getCause();
        }
        return cause;
    }
}