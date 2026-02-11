package ir.online.commons.stream.publisher.config;

import org.apache.kafka.clients.producer.ProducerInterceptor;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.clients.producer.RecordMetadata;
import org.slf4j.MDC;

import java.util.Map;
import java.util.UUID;

import static io.qoop.logs.LogKeys.CORRELATION_ID_HEADER;
import static io.qoop.logs.LogKeys.MDC_KEY;

public class CorrelationIdKafkaInterceptor implements ProducerInterceptor<String, Object> {

    @Override
    public ProducerRecord<String, Object> onSend(ProducerRecord<String, Object> record) {
        // Retrieve Correlation Id from MDC
        String correlationId = MDC.get(MDC_KEY);

        // If not present in MDC, generate a new one
        if (correlationId == null || correlationId.isEmpty()) {
            correlationId = UUID.randomUUID().toString();
        }

        // Add the header to the record
        record.headers().add(CORRELATION_ID_HEADER, correlationId.getBytes());

        return record;
    }

    @Override
    public void onAcknowledgement(RecordMetadata metadata, Exception exception) {
        // No implementation needed unless you want to log specific info after Kafka acknowledgement
    }

    @Override
    public void close() {
        // Close resources if necessary (left empty here)
    }

    @Override
    public void configure(Map<String, ?> configs) {
        // Initial configurations if necessary (left empty here)
    }
}