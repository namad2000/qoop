package io.qoop.outbox.scheduler;

import io.qoop.outbox.config.OutboxProperties;
import io.qoop.outbox.persistence.repository.OutboxEventJpaRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("Outbox Cleanup Scheduler Tests")
class OutboxCleanupSchedulerTest {

    @Mock
    private OutboxEventJpaRepository outboxEventRepository;

    @Mock
    private OutboxProperties outboxProperties;

    @Mock
    private OutboxProperties.Cleanup cleanup;

    private OutboxCleanupScheduler scheduler;

    @BeforeEach
    void setUp() {
        // Mock cleanup object
        lenient().when(outboxProperties.getCleanup()).thenReturn(cleanup);
        lenient().when(cleanup.getRetentionDays()).thenReturn(1);

        scheduler = new OutboxCleanupScheduler(outboxEventRepository, outboxProperties);
    }

    @Test
    @DisplayName("Should delete only SENT events in BATCH mode")
    void shouldDeleteOnlySentEventsInBatchMode() {
        // Given
        when(outboxProperties.getPublisherMode()).thenReturn("batch");

        // When
        scheduler.cleanup();

        // Then
        verify(outboxEventRepository, times(1)).deleteSentBefore(any(LocalDateTime.class));
        verify(outboxEventRepository, never()).deleteCreatedBefore(any(LocalDateTime.class));
    }

    @Test
    @DisplayName("Should delete ALL events in DEBEZIUM mode")
    void shouldDeleteAllEventsInDebeziumMode() {
        // Given
        when(outboxProperties.getPublisherMode()).thenReturn("debezium");

        // When
        scheduler.cleanup();

        // Then
        verify(outboxEventRepository, times(1)).deleteCreatedBefore(any(LocalDateTime.class));
        verify(outboxEventRepository, never()).deleteSentBefore(any(LocalDateTime.class));
    }

    @Test
    @DisplayName("Should use BATCH mode as default when mode is not set")
    void shouldUseBatchModeAsDefault() {
        // Given
        when(outboxProperties.getPublisherMode()).thenReturn("batch");

        // When
        scheduler.cleanup();

        // Then
        verify(outboxEventRepository, times(1)).deleteSentBefore(any(LocalDateTime.class));
        verify(outboxEventRepository, never()).deleteCreatedBefore(any(LocalDateTime.class));
    }

    @Test
    @DisplayName("Should delete events older than retention days")
    void shouldDeleteEventsOlderThanRetentionDays() {
        // Given
        when(outboxProperties.getPublisherMode()).thenReturn("batch");
        when(cleanup.getRetentionDays()).thenReturn(3);

        // When
        scheduler.cleanup();

        // Then
        verify(outboxEventRepository, times(1)).deleteSentBefore(any(LocalDateTime.class));
        verify(cleanup, atLeastOnce()).getRetentionDays();
    }

    @Test
    @DisplayName("Should use default retention days when cleanup is null")
    void shouldUseDefaultRetentionDaysWhenCleanupIsNull() {
        // Given
        when(outboxProperties.getPublisherMode()).thenReturn("batch");
        when(outboxProperties.getCleanup()).thenReturn(null);

        // When
        scheduler.cleanup();

        // Then
        verify(outboxEventRepository, times(1)).deleteSentBefore(any(LocalDateTime.class));
    }
}