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
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.Step;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.UUID;

/**
 * Dynamically creates and executes jobs at runtime.
 * Developer only defines Steps and Jobs normally.
 * This class handles partitioning and strategy selection automatically.
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
        log.info("Found job: {}", originalJob.getName());

        List<Step> jobSteps = extractStepsFromJob(originalJob);
        log.info("Found {} steps in job: {}", jobSteps.size(),
                jobSteps.stream().map(Step::getName).toList());

        if (jobSteps.isEmpty()) {
            throw new IllegalStateException("No steps found in job: " + jobName);
        }

        PartitionStrategy strategy = strategyFactory.createStrategy();
        log.info("Selected strategy: {}", strategy.getClass().getSimpleName());

        List<Step> wrappedSteps = new ArrayList<>();
        for (Step step : jobSteps) {
            Step wrappedStep = strategy.createPartitionStep(
                    step.getName() + "-partitioned",
                    jobRepository,
                    null,
                    step
            );
            wrappedSteps.add(wrappedStep);
            log.info("Wrapped step: {}", step.getName());
        }

        String dynamicJobName = jobName + "-" + UUID.randomUUID().toString().substring(0, 6);
        var jobBuilder = new JobBuilder(dynamicJobName, jobRepository)
                .start(wrappedSteps.get(0));

        for (int i = 1; i < wrappedSteps.size(); i++) {
            jobBuilder = jobBuilder.next(wrappedSteps.get(i));
        }

        Job dynamicJob = jobBuilder.build();
        log.info("Created dynamic job: {} with {} steps", dynamicJob.getName(), wrappedSteps.size());

        JobParametersBuilder params = new JobParametersBuilder()
                .addString("jobId", UUID.randomUUID().toString())
                .addString("nodeId", nodeIdentity.getNodeId())
                .addString("originalJobName", jobName)
                .addString("strategy", strategy.getClass().getSimpleName())
                .addLong("stepCount", (long) wrappedSteps.size())
                .addLong("timestamp", System.currentTimeMillis());

        log.info("Executing dynamic job: {}", dynamicJob.getName());

        return jobOperator.start(dynamicJob, params.toJobParameters());
    }

    /**
     * Executes job with default name.
     */
    @SneakyThrows
    public JobExecution runDynamicJob() {
        return runDynamicJob("defaultJob");
    }

    private Job getJobFromRegistry(String jobName) throws NoSuchJobException {
        try {
            return jobRegistry.getJob(jobName);
        } catch (Exception e) {
            throw new NoSuchJobException("Job not found in registry: " + jobName);
        }
    }

    /**
     * Extracts steps from a job by inspecting AbstractJob API, reflection, or Bean lookup fallback.
     */
    private List<Step> extractStepsFromJob(Job job) {
        List<Step> steps = new ArrayList<>();

        // 1. Inspect AbstractJob step names directly
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