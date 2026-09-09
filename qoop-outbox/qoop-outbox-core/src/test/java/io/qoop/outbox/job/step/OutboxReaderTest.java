package io.qoop.outbox.job.step;

import io.qoop.outbox.persistence.entity.OutboxEventEntity;
import io.qoop.outbox.persistence.repository.OutboxEventJpaRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("Outbox Reader Tests")
class OutboxReaderTest {

    @Mock
    private OutboxEventJpaRepository repository;

    private OutboxReader reader;

    @BeforeEach
    void setUp() {
        reader = new OutboxReader(repository, 100, 0, 4);
    }

    @Test
    @DisplayName("Should read events when batch is empty")
    void shouldReadEventsWhenBatchIsEmpty() {
        // Given
        List<OutboxEventEntity> events = List.of(
                createEvent(UUID.randomUUID()),
                createEvent(UUID.randomUUID())
        );
        when(repository.findNewForPartition(0, 4, 100)).thenReturn(events);

        // When
        OutboxEventEntity first = reader.read();
        OutboxEventEntity second = reader.read();

        // Then
        assertThat(first).isNotNull();
        assertThat(second).isNotNull();
        verify(repository, times(1)).findNewForPartition(0, 4, 100);
    }

    @Test
    @DisplayName("Should return null when no more events")
    void shouldReturnNullWhenNoMoreEvents() {
        // Given
        when(repository.findNewForPartition(0, 4, 100)).thenReturn(List.of());

        // When
        OutboxEventEntity result = reader.read();

        // Then
        assertThat(result).isNull();
        verify(repository, times(1)).findNewForPartition(0, 4, 100);
    }

    @Test
    @DisplayName("Should only query repository once for multiple reads")
    void shouldOnlyQueryRepositoryOnceForMultipleReads() {
        // Given
        List<OutboxEventEntity> events = List.of(
                createEvent(UUID.randomUUID()),
                createEvent(UUID.randomUUID()),
                createEvent(UUID.randomUUID())
        );
        when(repository.findNewForPartition(0, 4, 100)).thenReturn(events);

        // When
        reader.read();
        reader.read();
        reader.read();

        // Then
        verify(repository, times(1)).findNewForPartition(0, 4, 100);
    }

    @Test
    @DisplayName("Should use correct partition index and grid size")
    void shouldUseCorrectPartitionIndexAndGridSize() {
        // Given
        OutboxReader customReader = new OutboxReader(repository, 50, 2, 8);
        when(repository.findNewForPartition(2, 8, 50)).thenReturn(List.of());

        // When
        customReader.read();

        // Then
        verify(repository, times(1)).findNewForPartition(2, 8, 50);
    }

    private OutboxEventEntity createEvent(UUID id) {
        OutboxEventEntity event = new OutboxEventEntity();
        event.setId(id);
        return event;
    }
}