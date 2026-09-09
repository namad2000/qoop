package io.qoop.outbox.scheduler;

import io.qoop.batch.core.DynamicJobLauncher;
import io.qoop.cluster.NodeIdentity;
import io.qoop.outbox.config.OutboxProperties;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.batch.core.job.JobExecution;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("Outbox Job Scheduler Tests")
class OutboxJobSchedulerTest {

    @Mock
    private DynamicJobLauncher dynamicJobLauncher;

    @Mock
    private NodeIdentity nodeIdentity;

    @Mock
    private OutboxProperties outboxProperties;

    @Mock
    private JobExecution jobExecution;

    private OutboxJobScheduler scheduler;

    @BeforeEach
    void setUp() {
        when(nodeIdentity.getNodeId()).thenReturn("test-node-1");
        scheduler = new OutboxJobScheduler(dynamicJobLauncher, nodeIdentity, outboxProperties);
    }

    @Test
    @DisplayName("Should run job successfully")
    void shouldRunJobSuccessfully() throws Exception {
        // Given
        when(dynamicJobLauncher.runDynamicJob("outboxJob")).thenReturn(jobExecution);
        when(jobExecution.getId()).thenReturn(123L);

        // When
        scheduler.run();

        // Then
        verify(dynamicJobLauncher, times(1)).runDynamicJob("outboxJob");
    }

    @Test
    @DisplayName("Should throw RuntimeException when job fails")
    void shouldThrowRuntimeExceptionWhenJobFails() throws Exception {
        // Given
        when(dynamicJobLauncher.runDynamicJob("outboxJob"))
                .thenThrow(new RuntimeException("Job execution failed"));

        // When & Then
        assertThatThrownBy(() -> scheduler.run())
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Failed to start outboxJob");
    }
}