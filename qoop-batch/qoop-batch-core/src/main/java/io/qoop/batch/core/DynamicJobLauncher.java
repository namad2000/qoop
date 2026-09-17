package io.qoop.batch.core;

import io.qoop.batch.partition.factory.PartitionStrategyFactory;
import io.qoop.batch.partition.strategy.PartitionStrategy;
import io.qoop.cluster.NodeIdentity;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.configuration.JobRegistry;
import org.springframework.batch.core.job.AbstractJob;
import org.springframework.batch.core.job.Job;
import org.springframework.batch.core.job.JobExecution;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.job.parameters.JobParametersBuilder;
import org.springframework.batch.core.launch.JobOperator;
import org.springframework.batch.core.launch.NoSuchJobException;
import org.springframework.batch.core.partition.PartitionStep;
import org.springframework.batch.core.partition.Partitioner;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.Step;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.UUID;

/**
 * Dynamic job launcher that inspects jobs and dynamically wraps steps
 * with runtime-evaluated partition strategies (Local vs Remote).
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class DynamicJobLauncher {

    private final JobOperator jobOperator;
    private final JobRepository jobRepository;
    private final JobRegistry jobRegistry;
    private final ApplicationContext applicationContext;
    private final PartitionStrategyFactory strategyFactory;
    private final NodeIdentity nodeIdentity;

    /**
     * Executes a job dynamically with the given name.
     * Automatically detects cluster state and applies appropriate strategy.
     *
     * @param jobName Name of the job to execute
     * @return JobExecution result
     */
    @SneakyThrows
    public JobExecution runDynamicJob(String jobName) {
        log.info("=== Running dynamic job: {} ===", jobName);
        log.info("Node: {}", nodeIdentity.getNodeId());

        Job originalJob = getJobFromRegistry(jobName);
        List<Step> jobSteps = extractStepsFromJob(originalJob);

        if (jobSteps.isEmpty()) {
            throw new IllegalStateException("No steps found in job: " + jobName);
        }

        // Runtime Strategy Decision
        PartitionStrategy strategy = strategyFactory.createStrategy();
        log.info("Runtime strategy evaluated: {}", strategy.getClass().getSimpleName());

        List<Step> wrappedSteps = new ArrayList<>();
        for (Step step : jobSteps) {

            // Extract custom partitioner if explicitly defined in original step
            Partitioner customPartitioner = extractPartitionerFromStep(step);
            if (customPartitioner != null) {
                log.info("Custom Partitioner [{}] extracted from Step [{}]",
                        customPartitioner.getClass().getSimpleName(), step.getName());
            } else {
                log.info("No custom Partitioner found on Step [{}]. Applying strategy default.", step.getName());
            }

            Step wrappedStep = strategy.createPartitionStep(
                    step.getName() + "-partitioned",
                    jobRepository,
                    null,
                    step,
                    customPartitioner
            );
            wrappedSteps.add(wrappedStep);
        }

        String dynamicJobName = jobName + "-" + UUID.randomUUID().toString().substring(0, 6);
        var jobBuilder = new JobBuilder(dynamicJobName, jobRepository)
                .start(wrappedSteps.getFirst());

        for (int i = 1; i < wrappedSteps.size(); i++) {
            jobBuilder = jobBuilder.next(wrappedSteps.get(i));
        }

        Job dynamicJob = jobBuilder.build();

        JobParametersBuilder params = new JobParametersBuilder()
                .addString("jobId", UUID.randomUUID().toString())
                .addString("nodeId", nodeIdentity.getNodeId())
                .addString("originalJobName", jobName)
                .addString("strategy", strategy.getClass().getSimpleName())
                .addLong("timestamp", System.currentTimeMillis());

        return jobOperator.start(dynamicJob, params.toJobParameters());
    }

    /**
     * Inspects target Step to extract configured Partitioner instance.
     */
    private Partitioner extractPartitionerFromStep(Step step) {
        if (step == null) {
            return null;
        }

        if (step instanceof PartitionStep partitionStep) {
            try {
                Field partitionerField = PartitionStep.class.getDeclaredField("partitioner");
                partitionerField.setAccessible(true);
                return (Partitioner) partitionerField.get(partitionStep);
            } catch (Exception e) {
                log.debug("Direct reflection on PartitionStep field failed: {}", e.getMessage());
            }
        }

        try {
            Method getPartitionerMethod = step.getClass().getMethod("getPartitioner");
            Object result = getPartitionerMethod.invoke(step);
            if (result instanceof Partitioner partitioner) {
                return partitioner;
            }
        } catch (Exception ignored) {
        }

        return null;
    }

    private Job getJobFromRegistry(String jobName) throws NoSuchJobException {
        try {
            return jobRegistry.getJob(jobName);
        } catch (Exception e) {
            throw new NoSuchJobException("Job not found in registry: " + jobName);
        }
    }

    @SuppressWarnings("unchecked")
    private List<Step> extractStepsFromJob(Job job) {
        List<Step> steps = new ArrayList<>();

        if (job instanceof AbstractJob abstractJob) {
            Collection<String> stepNames = abstractJob.getStepNames();
            for (String stepName : stepNames) {
                Step step = abstractJob.getStep(stepName);
                if (step != null) {
                    steps.add(step);
                }
            }
        }

        if (steps.isEmpty()) {
            try {
                Method method = job.getClass().getMethod("getSteps");
                Object result = method.invoke(job);
                if (result instanceof List) {
                    steps = (List<Step>) result;
                }
            } catch (Exception e) {
                log.warn("Could not extract steps via reflection: {}", e.getMessage());
            }
        }

        if (steps.isEmpty()) {
            steps = findStepsByJobName(job.getName());
        }

        return steps;
    }

    private List<Step> findStepsByJobName(String jobName) {
        String[] stepBeanNames = applicationContext.getBeanNamesForType(Step.class);
        List<Step> steps = new ArrayList<>();

        for (String name : stepBeanNames) {
            if (name.toLowerCase().contains(jobName.toLowerCase())) {
                Step step = applicationContext.getBean(name, Step.class);
                steps.add(step);
            }
        }

        return steps;
    }
}