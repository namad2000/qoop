package io.qoop.batch.config;

import org.springframework.batch.core.job.JobExecution;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.StepExecution;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

@Component
public class BatchPayloadConverter {

    private final JobRepository jobRepository;

    public BatchPayloadConverter(JobRepository jobRepository) {
        this.jobRepository = jobRepository;
    }

    /**
     * Converts a heavy StepExecution object into a lightweight Map
     * to safely transfer it over Kafka without serialization depth issues.
     */
    public Object convertToMapIfNeeded(Object payload) {
        if (payload instanceof StepExecution stepExecution) {
            Map<String, Object> lightPayload = new HashMap<>();
            lightPayload.put("stepName", stepExecution.getStepName());
            lightPayload.put("stepExecutionId", stepExecution.getId());
            lightPayload.put("jobExecutionId", stepExecution.getJobExecutionId());
            lightPayload.put("executionContext", stepExecution.getExecutionContext().toMap());
            return lightPayload;
        }
        return payload;
    }

    /**
     * Reconstructs a StepExecution instance from a lightweight Map
     * received from Kafka.
     */
    public Object convertToStepExecutionIfNeeded(Object payload) {
        if (payload instanceof Map<?, ?> map) {
            String stepName = (String) map.get("stepName");
            Long stepExecutionId = map.get("stepExecutionId") != null ? ((Number) map.get("stepExecutionId")).longValue() : null;
            Long jobExecutionId = map.get("jobExecutionId") != null ? ((Number) map.get("jobExecutionId")).longValue() : null;

            JobExecution jobExecution = jobExecutionId != null ? jobRepository.getJobExecution(jobExecutionId) : null;

            StepExecution stepExecution = new StepExecution(stepExecutionId != null ? stepExecutionId : 0L, stepName, jobExecution);

            @SuppressWarnings("unchecked")
            Map<String, Object> ecMap = (Map<String, Object>) map.get("executionContext");
            if (ecMap != null) {
                for (Map.Entry<String, Object> entry : ecMap.entrySet()) {
                    stepExecution.getExecutionContext().put(entry.getKey(), entry.getValue());
                }
            }
            return stepExecution;
        }
        return payload;
    }
}