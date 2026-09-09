package io.qoop.batch.config;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.batch.core.configuration.JobRegistry;
import org.springframework.batch.core.launch.JobOperator;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.core.task.SimpleAsyncTaskExecutor;
import org.springframework.core.task.SyncTaskExecutor;
import org.springframework.core.task.TaskExecutor;
import org.springframework.test.util.ReflectionTestUtils;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.mock;

@DisplayName("Batch Configuration Tests")
class BatchConfigurationTest {

    @Test
    @DisplayName("Should create JobRegistry bean")
    void shouldCreateJobRegistry() {
        // Given
        BatchConfiguration config = new BatchConfiguration();

        // When
        JobRegistry jobRegistry = config.jobRegistry();

        // Then
        assertNotNull(jobRegistry);
        assertThat(jobRegistry).isInstanceOf(org.springframework.batch.core.configuration.support.MapJobRegistry.class);
    }

    @Test
    @DisplayName("Should create JobOperator bean with async executor by default")
    void shouldCreateJobOperatorWithAsyncExecutorByDefault() throws Exception {
        // Given
        BatchConfiguration config = new BatchConfiguration();
        JobRepository mockRepository = mock(JobRepository.class);
        JobRegistry mockRegistry = mock(JobRegistry.class);

        // When
        JobOperator jobOperator = config.jobOperator(mockRepository, mockRegistry);

        // Then
        assertNotNull(jobOperator);
        assertThat(jobOperator).isInstanceOf(org.springframework.batch.core.launch.support.TaskExecutorJobOperator.class);
    }

    @Test
    @DisplayName("Should return async TaskExecutor when type is async")
    void shouldReturnAsyncTaskExecutorWhenTypeIsAsync() {
        // Given
        BatchConfiguration config = new BatchConfiguration();
        ReflectionTestUtils.setField(config, "executorType", "async");

        // When
        TaskExecutor executor = config.getTaskExecutor();

        // Then
        assertNotNull(executor);
        assertThat(executor).isInstanceOf(SimpleAsyncTaskExecutor.class);
    }

    @Test
    @DisplayName("Should return sync TaskExecutor when type is sync")
    void shouldReturnSyncTaskExecutorWhenTypeIsSync() {
        // Given
        BatchConfiguration config = new BatchConfiguration();
        ReflectionTestUtils.setField(config, "executorType", "sync");

        // When
        TaskExecutor executor = config.getTaskExecutor();

        // Then
        assertNotNull(executor);
        assertThat(executor).isInstanceOf(SyncTaskExecutor.class);
    }

    @Test
    @DisplayName("Should use custom thread name prefix for async executor")
    void shouldUseCustomThreadNamePrefix() {
        // Given
        BatchConfiguration config = new BatchConfiguration();
        ReflectionTestUtils.setField(config, "executorType", "async");
        ReflectionTestUtils.setField(config, "threadNamePrefix", "custom-");

        // When
        SimpleAsyncTaskExecutor executor = (SimpleAsyncTaskExecutor) config.getTaskExecutor();

        // Then
        assertNotNull(executor);
        assertThat(executor).hasFieldOrPropertyWithValue("threadNamePrefix", "custom-");
    }

    @Test
    @DisplayName("Should use custom concurrency limit for async executor")
    void shouldUseCustomConcurrencyLimit() {
        // Given
        BatchConfiguration config = new BatchConfiguration();
        ReflectionTestUtils.setField(config, "executorType", "async");
        ReflectionTestUtils.setField(config, "concurrencyLimit", 20);

        // When
        SimpleAsyncTaskExecutor executor = (SimpleAsyncTaskExecutor) config.getTaskExecutor();

        // Then
        assertNotNull(executor);
        assertThat(executor).hasFieldOrPropertyWithValue("concurrencyLimit", 20);
    }
}