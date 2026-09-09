package io.qoop.outbox.job;

import io.qoop.outbox.config.OutboxProperties;
import io.qoop.outbox.job.step.OutboxProcessor;
import io.qoop.outbox.job.step.OutboxWriter;
import io.qoop.outbox.kafka.KafkaMessage;
import io.qoop.outbox.persistence.entity.OutboxEventEntity;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.job.Job;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.Step;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.infrastructure.item.ItemReader;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.transaction.PlatformTransactionManager;

/**
 * Main outbox batch configuration.
 *
 * <p>This configuration defines the job and the worker step.
 * The job is registered with name "outboxJob" so DynamicJobLauncher can discover it.</p>
 *
 * <p>Developer only defines the standard step with Reader, Processor, and Writer.
 * The DynamicJobLauncher handles partitioning, strategy selection, and job creation.</p>
 *
 * <p>The reader (PortBackedOutboxReader) is a prototype-scoped bean that receives
 * partition metadata from the step execution context via @Value injection.</p>
 */
@Slf4j
@Configuration
@ConditionalOnProperty(prefix = "outbox", name = "publisher-mode", havingValue = "batch", matchIfMissing = true)
@RequiredArgsConstructor
public class OutboxBatchConfig {

    private final OutboxProperties properties;

    /**
     * Creates the standard worker step with chunk-oriented processing.
     *
     * <p>Developer only defines this standard step.
     * DynamicJobLauncher will automatically wrap it with partitioning
     * and apply the appropriate strategy (LOCAL or HYBRID) at runtime.</p>
     *
     * @param jobRepository   Spring Batch job repository
     * @param txManager       Transaction manager
     * @param outboxReader    Reader for outbox events (prototype bean with @Value injection)
     * @param outboxProcessor Processor for transforming entities
     * @param outboxWriter    Writer for publishing messages
     * @return Configured Step instance
     */
    @Bean
    public Step outboxWorkerStep(JobRepository jobRepository,
                                 PlatformTransactionManager txManager,
                                 ItemReader<OutboxEventEntity> outboxReader,
                                 OutboxProcessor outboxProcessor,
                                 OutboxWriter outboxWriter) {

        log.info("Creating outbox worker step with chunkSize: {}", properties.getChunkSize());

        return new StepBuilder("outboxWorkerStep", jobRepository)
                .<OutboxEventEntity, KafkaMessage>chunk(properties.getChunkSize())
                .transactionManager(txManager)
                .reader(outboxReader)
                .processor(outboxProcessor)
                .writer(outboxWriter)
                .faultTolerant()
                // Retry on optimistic lock failures (concurrent processing)
                .retry(OptimisticLockingFailureException.class)
                .retry(org.hibernate.StaleObjectStateException.class)
                .retryLimit(2)

                // Skip if conflict persists across instances
                .skip(OptimisticLockingFailureException.class)
                .skip(org.hibernate.StaleObjectStateException.class)
                .skipLimit(100)
                .build();
    }

    /**
     * Creates the outbox job with the standard step.
     *
     * <p>The job is registered with name "outboxJob" so DynamicJobLauncher
     * can discover and execute it dynamically with appropriate partitioning.</p>
     *
     * @param jobRepository    Spring Batch job repository
     * @param outboxWorkerStep The standard worker step
     * @return Configured Job instance
     */
    @Bean("outboxJob")
    public Job outboxJob(JobRepository jobRepository, Step outboxWorkerStep) {
        log.info("Creating outbox job with worker step");

        return new JobBuilder("outboxJob", jobRepository)
                .start(outboxWorkerStep)
                .build();
    }
}