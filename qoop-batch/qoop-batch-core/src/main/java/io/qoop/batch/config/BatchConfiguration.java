package io.qoop.batch.config;

import org.springframework.batch.core.configuration.JobRegistry;
import org.springframework.batch.core.configuration.support.DefaultBatchConfiguration;
import org.springframework.batch.core.configuration.support.MapJobRegistry;
import org.springframework.batch.core.launch.JobOperator;
import org.springframework.batch.core.launch.support.TaskExecutorJobOperator;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.task.SimpleAsyncTaskExecutor;
import org.springframework.core.task.SyncTaskExecutor;
import org.springframework.core.task.TaskExecutor;

@Configuration
public class BatchConfiguration extends DefaultBatchConfiguration {

    @Value("${batch.task-executor.type:async}")
    private String executorType = "async";

    @Value("${batch.task-executor.thread-name-prefix:batch-}")
    private String threadNamePrefix = "batch-";

    @Value("${batch.task-executor.concurrency-limit:10}")
    private Integer concurrencyLimit = 10;

    @Override
    protected TaskExecutor getTaskExecutor() {
        if ("sync".equalsIgnoreCase(executorType)) {
            return new SyncTaskExecutor();
        }

        SimpleAsyncTaskExecutor executor = new SimpleAsyncTaskExecutor();
        executor.setThreadNamePrefix(threadNamePrefix != null ? threadNamePrefix : "batch-");
        executor.setConcurrencyLimit(concurrencyLimit != null ? concurrencyLimit : 10);
        return executor;
    }

    @Bean
    public JobRegistry jobRegistry() {
        return new MapJobRegistry();
    }

    @Bean
    public JobOperator jobOperator(JobRepository jobRepository, JobRegistry jobRegistry) throws Exception {
        TaskExecutorJobOperator operator = new TaskExecutorJobOperator();
        operator.setJobRepository(jobRepository);
        operator.setJobRegistry(jobRegistry);
        operator.setTaskExecutor(getTaskExecutor());
        operator.afterPropertiesSet();

        return operator;
    }
}