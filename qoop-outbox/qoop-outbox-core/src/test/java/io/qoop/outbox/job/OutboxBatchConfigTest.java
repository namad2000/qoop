package io.qoop.outbox.job;

import io.qoop.outbox.config.OutboxProperties;
import io.qoop.outbox.job.step.OutboxProcessor;
import io.qoop.outbox.job.step.OutboxWriter;
import io.qoop.outbox.persistence.entity.OutboxEventEntity;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.batch.core.job.Job;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.Step;
import org.springframework.batch.infrastructure.item.ItemReader;
import org.springframework.transaction.PlatformTransactionManager;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("Outbox Batch Config Tests")
class OutboxBatchConfigTest {

    @Mock
    private JobRepository jobRepository;

    @Mock
    private PlatformTransactionManager transactionManager;

    @Mock
    private ItemReader<OutboxEventEntity> outboxReader;

    @Mock
    private OutboxProcessor outboxProcessor;

    @Mock
    private OutboxWriter outboxWriter;

    @Mock
    private OutboxProperties properties;

    private OutboxBatchConfig config;

    @BeforeEach
    void setUp() {
        config = new OutboxBatchConfig(properties);
        when(properties.getChunkSize()).thenReturn(100);
    }

    @Test
    @DisplayName("Should create worker step with correct configuration")
    void shouldCreateWorkerStepWithCorrectConfiguration() {
        // When
        Step step = config.outboxWorkerStep(
                jobRepository,
                transactionManager,
                outboxReader,
                outboxProcessor,
                outboxWriter
        );

        // Then
        assertThat(step).isNotNull();
        assertThat(step.getName()).isEqualTo("outboxWorkerStep");
    }

    @Test
    @DisplayName("Should create outbox job with correct name")
    void shouldCreateOutboxJobWithCorrectName() {
        // Given
        Step step = config.outboxWorkerStep(
                jobRepository,
                transactionManager,
                outboxReader,
                outboxProcessor,
                outboxWriter
        );

        // When
        Job job = config.outboxJob(jobRepository, step);

        // Then
        assertThat(job).isNotNull();
        assertThat(job.getName()).isEqualTo("outboxJob");
    }
}