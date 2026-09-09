package io.qoop.outbox.service;

import io.qoop.outbox.persistence.entity.ErrorMessageEntity;
import io.qoop.outbox.persistence.repository.ErrorMessageJpaRepository;
import io.qoop.outbox.persistence.repository.OutboxEventJpaRepository;
import org.springframework.dao.OptimisticLockingFailureException;
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
    public void handleFailure(UUID eventId, Long version, ErrorMessageEntity errorMessage) {
        int updatedRows = outboxEventJpaRepository.markAsFailed(eventId, version);
        if (updatedRows == 0) {
            throw new OptimisticLockingFailureException("Optimistic lock failure: Outbox event already modified by another instance for ID: " + eventId);
        }
        errorMessageJpaRepository.save(errorMessage);
    }
}