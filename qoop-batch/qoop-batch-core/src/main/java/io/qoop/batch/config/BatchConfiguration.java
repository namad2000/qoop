package io.qoop.batch.config;

import lombok.RequiredArgsConstructor;
import org.springframework.batch.core.configuration.JobRegistry;
import org.springframework.batch.core.configuration.support.DefaultBatchConfiguration;
import org.springframework.batch.core.configuration.support.MapJobRegistry;
import org.springframework.batch.core.launch.JobOperator;
import org.springframework.batch.core.launch.support.TaskExecutorJobOperator;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.task.SimpleAsyncTaskExecutor;
import org.springframework.core.task.SyncTaskExecutor;
import org.springframework.core.task.TaskExecutor;

@Configuration
@RequiredArgsConstructor
public class BatchConfiguration extends DefaultBatchConfiguration {

    private final BatchKafkaProperties batchKafkaProperties;

    @Override
    protected TaskExecutor getTaskExecutor() {
        BatchKafkaProperties.TaskExecutor config = batchKafkaProperties.getTaskExecutor();
        String executorType = config.getType() != null ? config.getType() : "qoop-async";

        if ("sync".equalsIgnoreCase(executorType)) {
            return new SyncTaskExecutor();
        }

        SimpleAsyncTaskExecutor executor = new SimpleAsyncTaskExecutor();
        executor.setThreadNamePrefix(config.getThreadNamePrefix() != null ? config.getThreadNamePrefix() : "batch-");
        executor.setConcurrencyLimit(config.getConcurrencyLimit() != null ? config.getConcurrencyLimit() : 10);
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