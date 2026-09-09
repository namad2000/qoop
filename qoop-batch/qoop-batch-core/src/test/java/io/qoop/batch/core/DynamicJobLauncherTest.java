package io.qoop.batch.core;

import io.qoop.batch.partition.factory.PartitionStrategyFactory;
import io.qoop.batch.partition.strategy.PartitionStrategy;
import io.qoop.cluster.NodeIdentity;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.batch.core.configuration.JobRegistry;
import org.springframework.batch.core.job.Job;
import org.springframework.batch.core.job.JobExecution;
import org.springframework.batch.core.job.parameters.JobParameters;
import org.springframework.batch.core.launch.JobOperator;
import org.springframework.batch.core.launch.NoSuchJobException;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.Step;
import org.springframework.context.ApplicationContext;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("Dynamic Job Launcher Tests")
class DynamicJobLauncherTest {

    @Mock
    private JobOperator jobOperator;

    @Mock
    private JobRepository jobRepository;

    @Mock
    private JobRegistry jobRegistry;

    @Mock
    private ApplicationContext applicationContext;

    @Mock
    private PartitionStrategyFactory strategyFactory;

    @Mock
    private NodeIdentity nodeIdentity;

    @Mock
    private Job originalJob;

    @Mock
    private JobExecution jobExecution;

    @Mock
    private PartitionStrategy strategy;

    @Mock
    private Step step1;

    @Mock
    private Step step2;

    private DynamicJobLauncher launcher;

    @BeforeEach
    void setUp() {
        lenient().when(nodeIdentity.getNodeId()).thenReturn("test-node");
        lenient().when(step1.getName()).thenReturn("step1");
        lenient().when(step2.getName()).thenReturn("step2");

        launcher = new DynamicJobLauncher(
                jobOperator,
                jobRepository,
                jobRegistry,
                applicationContext,
                strategyFactory,
                nodeIdentity
        );
    }

    @Test
    @DisplayName("Should run dynamic job successfully")
    void shouldRunDynamicJobSuccessfully() throws Exception {
        // Given
        String jobName = "testJob";
        lenient().when(jobRegistry.getJob(jobName)).thenReturn(originalJob);
        lenient().when(originalJob.getName()).thenReturn(jobName);

        String[] stepBeanNames = {"testJob-step1"};
        lenient().when(applicationContext.getBeanNamesForType(Step.class)).thenReturn(stepBeanNames);
        lenient().when(applicationContext.getBean("testJob-step1", Step.class)).thenReturn(step1);

        lenient().when(strategyFactory.createStrategy()).thenReturn(strategy);
        lenient().when(strategy.createPartitionStep(anyString(), any(), any(), any())).thenReturn(step1);

        lenient().when(jobOperator.start(any(Job.class), any(JobParameters.class))).thenReturn(jobExecution);
        lenient().when(jobExecution.getId()).thenReturn(123L);

        // When
        JobExecution result = launcher.runDynamicJob(jobName);

        // Then
        assertNotNull(result);
        assertThat(result.getId()).isEqualTo(123L);
        verify(applicationContext).getBeanNamesForType(Step.class);
    }

    @Test
    @DisplayName("Should throw RuntimeException when job not found in registry")
    void shouldThrowRuntimeExceptionWhenJobNotFound() throws Exception {
        // Given
        String jobName = "nonExistentJob";
        when(jobRegistry.getJob(jobName)).thenThrow(new RuntimeException("Job not found"));

        // When & Then
        assertThatThrownBy(() -> launcher.runDynamicJob(jobName))
                .isInstanceOf(NoSuchJobException.class)
                .hasMessageContaining("Job not found in registry");
    }

    @Test
    @DisplayName("Should throw IllegalStateException when no steps found in job")
    void shouldThrowIllegalStateExceptionWhenNoStepsFound() throws Exception {
        // Given
        String jobName = "emptyJob";
        lenient().when(jobRegistry.getJob(jobName)).thenReturn(originalJob);
        lenient().when(originalJob.getName()).thenReturn(jobName);

        String[] stepBeanNames = {};
        lenient().when(applicationContext.getBeanNamesForType(Step.class)).thenReturn(stepBeanNames);

        // When & Then
        assertThatThrownBy(() -> launcher.runDynamicJob(jobName))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("No steps found in job");
    }

    @Test
    @DisplayName("Should run with default job name when none provided")
    void shouldRunWithDefaultJobName() throws Exception {
        // Given
        String defaultJobName = "defaultJob";
        lenient().when(jobRegistry.getJob(defaultJobName)).thenReturn(originalJob);
        lenient().when(originalJob.getName()).thenReturn(defaultJobName);

        String[] stepBeanNames = {"defaultJob-step1"};
        lenient().when(applicationContext.getBeanNamesForType(Step.class)).thenReturn(stepBeanNames);
        lenient().when(applicationContext.getBean("defaultJob-step1", Step.class)).thenReturn(step1);

        lenient().when(strategyFactory.createStrategy()).thenReturn(strategy);
        lenient().when(strategy.createPartitionStep(anyString(), any(), any(), any())).thenReturn(step1);
        lenient().when(jobOperator.start(any(Job.class), any(JobParameters.class))).thenReturn(jobExecution);

        // When
        launcher.runDynamicJob();

        // Then
        verify(jobRegistry).getJob(defaultJobName);
    }

    @Test
    @DisplayName("Should rethrow RuntimeException from JobOperator with message")
    void shouldRethrowRuntimeExceptionFromJobOperator() throws Exception {
        // Given
        String jobName = "runningJob";
        lenient().when(jobRegistry.getJob(jobName)).thenReturn(originalJob);
        lenient().when(originalJob.getName()).thenReturn(jobName);

        String[] stepBeanNames = {"runningJob-step1"};
        lenient().when(applicationContext.getBeanNamesForType(Step.class)).thenReturn(stepBeanNames);
        lenient().when(applicationContext.getBean("runningJob-step1", Step.class)).thenReturn(step1);

        lenient().when(strategyFactory.createStrategy()).thenReturn(strategy);
        lenient().when(strategy.createPartitionStep(anyString(), any(), any(), any())).thenReturn(step1);

        when(jobOperator.start(any(Job.class), any(JobParameters.class)))
                .thenThrow(new RuntimeException("Job execution failed"));

        // When & Then
        assertThatThrownBy(() -> launcher.runDynamicJob(jobName))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("Job execution failed");
    }

    @Test
    @DisplayName("Should find steps by name fallback when reflection fails")
    void shouldFindStepsByNameFallbackWhenReflectionFails() throws Exception {
        // Given
        String jobName = "fallbackJob";
        lenient().when(jobRegistry.getJob(jobName)).thenReturn(originalJob);
        lenient().when(originalJob.getName()).thenReturn(jobName);

        String[] stepBeanNames = {"fallbackJob-step1", "fallbackJob-step2"};
        lenient().when(applicationContext.getBeanNamesForType(Step.class)).thenReturn(stepBeanNames);
        lenient().when(applicationContext.getBean("fallbackJob-step1", Step.class)).thenReturn(step1);
        lenient().when(applicationContext.getBean("fallbackJob-step2", Step.class)).thenReturn(step2);

        lenient().when(strategyFactory.createStrategy()).thenReturn(strategy);
        lenient().when(strategy.createPartitionStep(anyString(), any(), any(), any())).thenReturn(step1);
        lenient().when(jobOperator.start(any(Job.class), any(JobParameters.class))).thenReturn(jobExecution);

        // When
        JobExecution result = launcher.runDynamicJob(jobName);

        // Then
        assertNotNull(result);
        verify(applicationContext).getBeanNamesForType(Step.class);
    }

    @Test
    @DisplayName("Should rethrow RuntimeException from JobOperator with custom message")
    void shouldRethrowRuntimeExceptionFromJobOperatorWithCustomMessage() throws Exception {
        // Given
        String jobName = "exceptionJob";
        lenient().when(jobRegistry.getJob(jobName)).thenReturn(originalJob);
        lenient().when(originalJob.getName()).thenReturn(jobName);

        String[] stepBeanNames = {"exceptionJob-step1"};
        lenient().when(applicationContext.getBeanNamesForType(Step.class)).thenReturn(stepBeanNames);
        lenient().when(applicationContext.getBean("exceptionJob-step1", Step.class)).thenReturn(step1);

        lenient().when(strategyFactory.createStrategy()).thenReturn(strategy);
        lenient().when(strategy.createPartitionStep(anyString(), any(), any(), any())).thenReturn(step1);

        when(jobOperator.start(any(Job.class), any(JobParameters.class)))
                .thenThrow(new RuntimeException("Unexpected error"));

        // When & Then
        assertThatThrownBy(() -> launcher.runDynamicJob(jobName))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("Unexpected error");
    }
}