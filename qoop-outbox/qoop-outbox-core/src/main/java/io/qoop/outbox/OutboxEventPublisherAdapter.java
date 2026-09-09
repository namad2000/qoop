package io.qoop.outbox;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.qoop.outbox.mapper.OutboxEventMapper;
import io.qoop.outbox.persistence.entity.OutboxEventEntity;
import io.qoop.outbox.persistence.repository.OutboxEventJpaRepository;
import io.qoop.outbox.validator.OutboxMetadataValidator;
import io.qoop.stream.api.Header;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Author: davood akbari
 * Email: daak1365@gmail.com
 * Created: 9/2/2026 5:18 PM
 * Package: io.qoop.outbox
 */
@Service
@RequiredArgsConstructor
public class OutboxEventPublisherAdapter implements OutboxEventPublisher {

    private final OutboxEventJpaRepository outboxEventJpaRepository;
    private final OutboxMetadataValidator metadataValidator;
    private final AggregateIdExtractor aggregateIdExtractor;
    private final OutboxEventMapper outboxEventMapper;
    private final ObjectMapper objectMapper;

    @Override
    @Transactional
    public void publish(Object payload) {
        publish(null, payload);
    }

    @Override
    @Transactional
    public void publish(List<Header> explicitHeaders, Object payload) {
        OutBoxEvent metadata = metadataValidator.validateAndGetAnnotation(payload);
        String aggregateId = aggregateIdExtractor.extract(payload);

        // Mapping using MapStruct interface
        OutboxEventEntity entity = outboxEventMapper.toEntity(
                payload,
                aggregateId,
                metadata,
                explicitHeaders,
                objectMapper
        );

        persistEvent(entity);
    }

    private void persistEvent(OutboxEventEntity entity) {
        outboxEventJpaRepository.save(entity);
    }
}