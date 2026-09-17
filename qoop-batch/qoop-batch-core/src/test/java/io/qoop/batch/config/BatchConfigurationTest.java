package io.qoop.batch.config;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.batch.core.configuration.JobRegistry;
import org.springframework.batch.core.launch.JobOperator;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.core.task.SimpleAsyncTaskExecutor;
import org.springframework.core.task.SyncTaskExecutor;
import org.springframework.core.task.TaskExecutor;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.mock;

@DisplayName("Batch Configuration Tests")
class BatchConfigurationTest {

    private BatchKafkaProperties batchKafkaProperties;
    private BatchConfiguration config;

    @BeforeEach
    void setUp() {
        batchKafkaProperties = new BatchKafkaProperties();
        config = new BatchConfiguration(batchKafkaProperties);
    }

    @Test
    @DisplayName("Should create JobRegistry bean")
    void shouldCreateJobRegistry() {
        JobRegistry jobRegistry = config.jobRegistry();

        assertNotNull(jobRegistry);
        assertThat(jobRegistry).isInstanceOf(org.springframework.batch.core.configuration.support.MapJobRegistry.class);
    }

    @Test
    @DisplayName("Should create JobOperator bean with async executor by default")
    void shouldCreateJobOperatorWithAsyncExecutorByDefault() throws Exception {
        JobRepository mockRepository = mock(JobRepository.class);
        JobRegistry mockRegistry = mock(JobRegistry.class);

        JobOperator jobOperator = config.jobOperator(mockRepository, mockRegistry);

        assertNotNull(jobOperator);
        assertThat(jobOperator).isInstanceOf(org.springframework.batch.core.launch.support.TaskExecutorJobOperator.class);
    }

    @Test
    @DisplayName("Should return async TaskExecutor when type is async")
    void shouldReturnAsyncTaskExecutorWhenTypeIsAsync() {
        batchKafkaProperties.getTaskExecutor().setType("async");

        TaskExecutor executor = config.getTaskExecutor();

        assertNotNull(executor);
        assertThat(executor).isInstanceOf(SimpleAsyncTaskExecutor.class);
    }

    @Test
    @DisplayName("Should return sync TaskExecutor when type is sync")
    void shouldReturnSyncTaskExecutorWhenTypeIsSync() {
        batchKafkaProperties.getTaskExecutor().setType("sync");

        TaskExecutor executor = config.getTaskExecutor();

        assertNotNull(executor);
        assertThat(executor).isInstanceOf(SyncTaskExecutor.class);
    }

    @Test
    @DisplayName("Should use custom thread name prefix for async executor")
    void shouldUseCustomThreadNamePrefix() {
        batchKafkaProperties.getTaskExecutor().setType("async");
        batchKafkaProperties.getTaskExecutor().setThreadNamePrefix("custom-");

        SimpleAsyncTaskExecutor executor = (SimpleAsyncTaskExecutor) config.getTaskExecutor();

        assertNotNull(executor);
        assertThat(executor).hasFieldOrPropertyWithValue("threadNamePrefix", "custom-");
    }

    @Test
    @DisplayName("Should use custom concurrency limit for async executor")
    void shouldUseCustomConcurrencyLimit() {
        batchKafkaProperties.getTaskExecutor().setType("async");
        batchKafkaProperties.getTaskExecutor().setConcurrencyLimit(20);

        SimpleAsyncTaskExecutor executor = (SimpleAsyncTaskExecutor) config.getTaskExecutor();

        assertNotNull(executor);
        assertThat(executor).hasFieldOrPropertyWithValue("concurrencyLimit", 20);
    }
}