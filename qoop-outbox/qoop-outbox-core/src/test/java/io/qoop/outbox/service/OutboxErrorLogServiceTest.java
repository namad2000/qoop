package io.qoop.outbox.service;

import io.qoop.outbox.persistence.entity.ErrorMessageEntity;
import io.qoop.outbox.persistence.repository.ErrorMessageJpaRepository;
import io.qoop.outbox.persistence.repository.OutboxEventJpaRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.OptimisticLockingFailureException;

import java.util.UUID;

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

        when(outboxEventJpaRepository.markAsFailed(eventId, version)).thenReturn(1);

        // When
        service.handleFailure(eventId, version, errorMessage);

        // Then
        verify(outboxEventJpaRepository, times(1)).markAsFailed(eventId, version);
        verify(errorMessageJpaRepository, times(1)).save(errorMessage);
    }

    @Test
    @DisplayName("Should throw OptimisticLockingFailureException when update fails")
    void shouldThrowOptimisticLockingFailureExceptionWhenUpdateFails() {
        // Given
        UUID eventId = UUID.randomUUID();
        Long version = 0L;
        ErrorMessageEntity errorMessage = new ErrorMessageEntity();

        when(outboxEventJpaRepository.markAsFailed(eventId, version)).thenReturn(0);

        // When & Then
        assertThatThrownBy(() -> service.handleFailure(eventId, version, errorMessage))
                .isInstanceOf(OptimisticLockingFailureException.class)
                .hasMessageContaining("Optimistic lock failure");

        verify(outboxEventJpaRepository, times(1)).markAsFailed(eventId, version);
        verify(errorMessageJpaRepository, never()).save(any());
    }
}