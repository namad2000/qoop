package io.qoop.batch.config;

import com.fasterxml.jackson.annotation.JsonIgnore;
import org.springframework.batch.core.job.JobExecution;
import org.springframework.batch.core.job.JobInstance;
import org.springframework.batch.core.step.StepExecution;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import tools.jackson.core.StreamWriteConstraints;
import tools.jackson.core.json.JsonFactory;
import tools.jackson.databind.MapperFeature;
import tools.jackson.databind.SerializationFeature;
import tools.jackson.databind.json.JsonMapper;

import java.util.List;

@Configuration
public class KafkaJacksonConfig {

    /**
     * StepExecution -> JobExecution -> JobInstance -> jobExecutions[] -> JobExecution -> ...
     * This is the real object cycle that Batch 6's domain model creates.
     * Ignoring "jobExecution" on StepExecution cuts the cycle at its actual
     * entry point (this is the object that gets published to Kafka by
     * MessageChannelPartitionHandler replies).
     */
    private abstract static class StepExecutionMixin {
        @JsonIgnore
        abstract JobExecution getJobExecution();
    }

    /**
     * Defensive: prevents the same cycle if JobExecution is ever serialized directly.
     */
    private abstract static class JobExecutionMixin {
        @JsonIgnore
        abstract JobInstance getJobInstance();
    }

    /**
     * Defensive: prevents the same cycle if JobInstance is ever serialized directly.
     */
    private abstract static class JobInstanceMixin {
        @JsonIgnore
        abstract List<JobExecution> getJobExecutions();
    }

    @Bean
    @Primary
    public JsonMapper jsonMapper() {

        // In Jackson 3.x (the "tools.jackson" package), StreamWriteConstraints
        // is a property of the underlying JsonFactory, NOT of JsonMapper.builder()
        // directly. It must be built into the JsonFactory first, then that factory
        // is handed to JsonMapper.builder(factory). This is why
        // JsonMapper.builder().streamWriteConstraints(...) does not compile/exist.
        JsonFactory jsonFactory = JsonFactory.builder()
                .streamWriteConstraints(
                        StreamWriteConstraints.builder()
                                .maxNestingDepth(2000)
                                .build()
                )
                .build();

        return JsonMapper.builder(jsonFactory)
                .disable(SerializationFeature.FAIL_ON_EMPTY_BEANS)
                .disable(SerializationFeature.FAIL_ON_SELF_REFERENCES)
                // Must be ENABLED for the @JsonIgnore mixins below to take effect.
                .enable(MapperFeature.USE_ANNOTATIONS)
                // Cut the circular reference at its real entry point.
                .addMixIn(StepExecution.class, StepExecutionMixin.class)
                .addMixIn(JobExecution.class, JobExecutionMixin.class)
                .addMixIn(JobInstance.class, JobInstanceMixin.class)
                .build();
    }
}