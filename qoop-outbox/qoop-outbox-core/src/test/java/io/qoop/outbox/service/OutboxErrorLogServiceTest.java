package io.qoop.outbox.service;

import io.qoop.outbox.OutboxStatus;
import io.qoop.outbox.persistence.entity.ErrorMessageEntity;
import io.qoop.outbox.persistence.entity.OutboxEventEntity;
import io.qoop.outbox.persistence.repository.ErrorMessageJpaRepository;
import io.qoop.outbox.persistence.repository.OutboxEventJpaRepository;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.orm.ObjectOptimisticLockingFailureException;

import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("Outbox Error Log Service Tests")
class OutboxErrorLogServiceTest {

    @Mock
    private ErrorMessageJpaRepository errorMessageJpaRepository;

    @Mock
    private OutboxEventJpaRepository outboxEventJpaRepository;

    @InjectMocks
    private OutboxErrorLogService service;

    @Test
    @DisplayName("Should handle failure successfully")
    void shouldHandleFailureSuccessfully() {
        // Given
        UUID eventId = UUID.randomUUID();
        Long version = 0L;
        ErrorMessageEntity errorMessage = new ErrorMessageEntity();

        OutboxEventEntity entity = new OutboxEventEntity();
        entity.setId(eventId);
        entity.setStatus(OutboxStatus.NEW);
        entity.setRetryCount(0);

        when(outboxEventJpaRepository.findById(eventId)).thenReturn(Optional.of(entity));
        when(outboxEventJpaRepository.save(any(OutboxEventEntity.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // When
        service.handleFailure(eventId, version, errorMessage);

        // Then
        verify(outboxEventJpaRepository, times(1)).findById(eventId);
        verify(outboxEventJpaRepository, times(1)).save(entity);
        verify(errorMessageJpaRepository, times(1)).save(errorMessage);

        assertThat(entity.getStatus()).isEqualTo(OutboxStatus.FAILED);
        assertThat(entity.getRetryCount()).isEqualTo(1);
    }

    @Test
    @DisplayName("Should throw ObjectOptimisticLockingFailureException when save fails due to version mismatch")
    void shouldThrowOptimisticLockingFailureExceptionWhenUpdateFails() {
        // Given
        UUID eventId = UUID.randomUUID();
        Long version = 0L;
        ErrorMessageEntity errorMessage = new ErrorMessageEntity();

        OutboxEventEntity entity = new OutboxEventEntity();
        entity.setId(eventId);
        entity.setRetryCount(0);

        when(outboxEventJpaRepository.findById(eventId)).thenReturn(Optional.of(entity));
        // Simulate Hibernate optimistic lock failure
        doThrow(new ObjectOptimisticLockingFailureException(OutboxEventEntity.class, eventId))
                .when(outboxEventJpaRepository).save(any(OutboxEventEntity.class));

        // When & Then
        assertThatThrownBy(() -> service.handleFailure(eventId, version, errorMessage))
                .isInstanceOf(ObjectOptimisticLockingFailureException.class);

        verify(outboxEventJpaRepository, times(1)).findById(eventId);
        verify(outboxEventJpaRepository, times(1)).save(entity);
        verify(errorMessageJpaRepository, never()).save(any());
    }

    @Test
    @DisplayName("Should throw EntityNotFoundException when event does not exist")
    void shouldThrowEntityNotFoundExceptionWhenEventNotFound() {
        // Given
        UUID eventId = UUID.randomUUID();
        Long version = 0L;
        ErrorMessageEntity errorMessage = new ErrorMessageEntity();

        when(outboxEventJpaRepository.findById(eventId)).thenReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> service.handleFailure(eventId, version, errorMessage))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessageContaining("Outbox event not found for ID: " + eventId);

        verify(outboxEventJpaRepository, times(1)).findById(eventId);
        verify(outboxEventJpaRepository, never()).save(any());
        verify(errorMessageJpaRepository, never()).save(any());
    }
}