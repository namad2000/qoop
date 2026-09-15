package io.qoop.outbox.service;

import io.qoop.outbox.OutboxStatus;
import io.qoop.outbox.persistence.entity.ErrorMessageEntity;
import io.qoop.outbox.persistence.entity.OutboxEventEntity;
import io.qoop.outbox.persistence.repository.ErrorMessageJpaRepository;
import io.qoop.outbox.persistence.repository.OutboxEventJpaRepository;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
public class OutboxErrorLogService {

    private final ErrorMessageJpaRepository errorMessageJpaRepository;
    private final OutboxEventJpaRepository outboxEventJpaRepository;

    public OutboxErrorLogService(ErrorMessageJpaRepository errorMessageJpaRepository,
                                 OutboxEventJpaRepository outboxEventJpaRepository) {
        this.errorMessageJpaRepository = errorMessageJpaRepository;
        this.outboxEventJpaRepository = outboxEventJpaRepository;
    }

    // Executes in a completely independent transaction and enforces version check
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void handleFailure(UUID eventId, Long expectedVersion, ErrorMessageEntity errorMessage) {
        OutboxEventEntity entity = outboxEventJpaRepository.findById(eventId)
                .orElseThrow(() -> new EntityNotFoundException("Outbox event not found for ID: " + eventId));

        entity.setStatus(OutboxStatus.FAILED);
        entity.setRetryCount(entity.getRetryCount() + 1);

        outboxEventJpaRepository.save(entity);
        errorMessageJpaRepository.save(errorMessage);
    }
}