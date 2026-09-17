package io.qoop.batch.partition.strategy;

import io.qoop.batch.config.*;
import io.qoop.batch.core.DynamicJobLauncher;
import io.qoop.batch.partition.factory.PartitionStrategyFactory;
import io.qoop.cluster.ClusterNodeCounter;
import io.qoop.cluster.ClusterRoleDetector;
import io.qoop.cluster.NodeIdentity;
import io.qoop.stream.publisher.config.KafkaPublisherConfig;
import io.qoop.stream.starter.KafkaProperties;
import io.qoop.stream.subscriber.config.KafkaSubscriberConfig;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.redisson.api.RedissonClient;
import org.springframework.batch.core.BatchStatus;
import org.springframework.batch.core.job.Job;
import org.springframework.batch.core.job.JobExecution;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.job.parameters.JobParameters;
import org.springframework.batch.core.job.parameters.JobParametersBuilder;
import org.springframework.batch.core.launch.JobOperator;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.Step;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.batch.infrastructure.item.ExecutionContext;
import org.springframework.batch.infrastructure.repeat.RepeatStatus;
import org.springframework.batch.infrastructure.support.transaction.ResourcelessTransactionManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.kafka.autoconfigure.KafkaAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.kafka.listener.DefaultErrorHandler;
import org.springframework.kafka.test.context.EmbeddedKafka;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.transaction.PlatformTransactionManager;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Stream;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

@SpringBootTest(
        classes = {
                BatchPayloadConverter.class,
                KafkaJacksonConfig.class,
                KafkaPublisherConfig.class,
                KafkaSubscriberConfig.class,
                BatchKafkaProperties.class,
                KafkaProperties.class,
                KafkaAutoConfiguration.class,
                BatchConfiguration.class,
                BatchKafkaIntegrationConfig.class,
                BatchWorkerConfiguration.class,
                PartitionStrategyFactory.class,
                DynamicJobLauncher.class,
                PartitionStrategyIntegrationTest.TestBatchConfig.class
        },
        properties = {
                "spring.application.name=app",
                "event.stream.bootstrap-servers=${spring.embedded.kafka.brokers}",
                "batch.partition.strategy.force-local=false",
                "batch.partition.grid-size=2",
                "batch.partition.chunk-size=5",
                "batch.kafka.enabled=true",
                "batch.kafka.namespace=default",
                "batch.kafka.request-topic=qoop-batch-requests",
                "batch.kafka.reply-topic=qoop-batch-replies"
        }
)
@EmbeddedKafka(
        partitions = 2,
        topics = {"qoop-batch.default.qoop-batch-requests", "qoop-batch.default.qoop-batch-replies"}
)
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
class PartitionStrategyIntegrationTest {

    @Autowired
    private JobOperator jobOperator;

    @Autowired
    private JobRepository jobRepository;

    @Autowired
    private PlatformTransactionManager transactionManager;

    @Autowired
    private PartitionStrategyFactory partitionStrategyFactory;

    @Autowired
    private DynamicJobLauncher dynamicJobLauncher;

    @Autowired
    private Tasklet sampleTasklet;

    @Autowired
    private Step baseStep;

    @MockitoBean
    private NodeIdentity nodeIdentity;

    @MockitoBean
    private ClusterNodeCounter nodeCounter;

    @MockitoBean
    private ClusterRoleDetector roleDetector;

    @MockitoBean
    private RedisTemplate<String, Object> redisTemplate;

    @MockitoBean
    private RedissonClient redissonClient;

    private static final AtomicInteger executedTasks = new AtomicInteger(0);

    @TestConfiguration
    static class TestBatchConfig {

        @Bean
        public DefaultErrorHandler defaultErrorHandler() {
            return new DefaultErrorHandler();
        }

        @Bean
        public PlatformTransactionManager transactionManager() {
            return new ResourcelessTransactionManager();
        }

        @Bean
        public Tasklet sampleTasklet() {
            return (contribution, chunkContext) -> {
                executedTasks.incrementAndGet();
                return RepeatStatus.FINISHED;
            };
        }

        @Bean
        public Step baseStep(JobRepository jobRepository, PlatformTransactionManager transactionManager, Tasklet sampleTasklet) {
            return new StepBuilder("baseStep", jobRepository)
                    .tasklet(sampleTasklet, transactionManager)
                    .build();
        }

        @Bean
        public Job registeredTestJob(JobRepository jobRepository, Step baseStep) {
            return new JobBuilder("registeredTestJob", jobRepository)
                    .start(baseStep)
                    .build();
        }
    }

    @BeforeEach
    void setUp() {
        executedTasks.set(0);
        when(nodeIdentity.getNodeId()).thenReturn("local-node");
        when(nodeCounter.scanNodeKeys()).thenAnswer(invocation -> Stream.of("qoop:app:cluster:node:local-node"));
        when(roleDetector.isMaster(anyString())).thenReturn(true);
    }

    private JobExecution awaitJobCompletion(JobExecution initialExecution) throws InterruptedException {
        JobExecution execution = initialExecution;
        long timeoutMs = System.currentTimeMillis() + 15000;

        while (execution.getStatus().isRunning() || execution.getStatus() == BatchStatus.STARTING) {
            if (System.currentTimeMillis() > timeoutMs) {
                break;
            }
            Thread.sleep(200);
            execution = jobRepository.getJobExecution(execution.getId());
        }
        return execution;
    }

    @Test
    @DisplayName("Local Strategy Test: Verifies Local Partitioning with Custom Partitioner")
    void testLocalStrategyExecution() throws Exception {
        when(nodeIdentity.getNodeId()).thenReturn("local-node");
        when(nodeCounter.scanNodeKeys()).thenAnswer(invocation -> Stream.of("qoop:app:cluster:node:local-node"));

        PartitionStrategy localStrategy = partitionStrategyFactory.createStrategy();

        Step workerStep = new StepBuilder("localWorkerStep", jobRepository)
                .tasklet(sampleTasklet, transactionManager)
                .build();

        Step partitionStep = localStrategy.createPartitionStep(
                "localPartitionStep",
                jobRepository,
                transactionManager,
                workerStep,
                gridSize -> {
                    var map = new ConcurrentHashMap<String, ExecutionContext>();
                    map.put("p0", new ExecutionContext());
                    map.put("p1", new ExecutionContext());
                    return map;
                }
        );

        Job job = new JobBuilder("localJob", jobRepository)
                .start(partitionStep)
                .build();

        JobParameters params = new JobParametersBuilder()
                .addLong("time", System.currentTimeMillis())
                .toJobParameters();

        JobExecution execution = awaitJobCompletion(jobOperator.start(job, params));

        assertEquals(BatchStatus.COMPLETED, execution.getStatus());
        assertEquals(2, executedTasks.get());
    }

    @Test
    @DisplayName("Hybrid Strategy Test: End-to-End Master/Worker communication over Embedded Kafka")
    void testHybridStrategyWithEmbeddedKafka() throws Exception {
        when(nodeIdentity.getNodeId()).thenReturn("node-master");
        when(roleDetector.isMaster(anyString())).thenReturn(true);
        when(nodeCounter.scanNodeKeys()).thenAnswer(invocation -> Stream.of("node-master", "node-worker-1").map(n -> "qoop:app:cluster:node:" + n));

        PartitionStrategy hybridStrategy = partitionStrategyFactory.createStrategy();

        Step masterStep = hybridStrategy.createPartitionStep(
                "hybridMasterStep",
                jobRepository,
                transactionManager,
                baseStep
        );

        Job job = new JobBuilder("hybridKafkaJob", jobRepository)
                .start(masterStep)
                .build();

        JobParameters params = new JobParametersBuilder()
                .addLong("time", System.currentTimeMillis())
                .toJobParameters();

        JobExecution execution = awaitJobCompletion(jobOperator.start(job, params));

        if (execution.getStatus() != BatchStatus.COMPLETED) {
            execution.getStepExecutions().forEach(stepExec -> {
                System.err.println(">>> FAILED STEP: " + stepExec.getStepName() +
                                   " | Status: " + stepExec.getStatus() +
                                   " | ExitStatus: " + stepExec.getExitStatus() +
                                   " | FailureExceptions: " + stepExec.getFailureExceptions());
            });
        }

        assertThat(execution.getStatus())
                .as("Job execution should complete successfully")
                .isEqualTo(BatchStatus.COMPLETED);

        assertThat(executedTasks.get())
                .as("Executed tasks count should match partition count")
                .isEqualTo(hybridStrategy.getPartitionCount());
    }

    @Test
    @DisplayName("Multi-Node Cluster Simulation Test: Multiple distributed workers processing partitions")
    void testMultiNodeClusterSimulation() throws Exception {
        when(nodeIdentity.getNodeId()).thenReturn("node-master");
        when(roleDetector.isMaster(anyString())).thenReturn(true);
        when(nodeCounter.scanNodeKeys()).thenAnswer(invocation ->
                Stream.of("node-master", "node-worker-1", "node-worker-2", "node-worker-3")
                        .map(n -> "qoop:app:cluster:node:" + n)
        );

        PartitionStrategy hybridStrategy = partitionStrategyFactory.createStrategy();

        Step masterStep = hybridStrategy.createPartitionStep(
                "multiNodeMasterStep",
                jobRepository,
                transactionManager,
                baseStep
        );

        Job job = new JobBuilder("multiNodeClusterJob", jobRepository)
                .start(masterStep)
                .build();

        JobParameters params = new JobParametersBuilder()
                .addLong("time", System.currentTimeMillis())
                .toJobParameters();

        JobExecution execution = awaitJobCompletion(jobOperator.start(job, params));

        org.assertj.core.api.Assertions.assertThat(execution.getStatus())
                .as("Multi-node cluster job execution should complete successfully")
                .isEqualTo(BatchStatus.COMPLETED);
    }

    @Test
    @DisplayName("DynamicJobLauncher Test: Runs registered Job through dynamic pipeline")
    void testDynamicJobLauncherRunDynamicJob() throws Exception {
        when(nodeIdentity.getNodeId()).thenReturn("local-node");
        when(nodeCounter.scanNodeKeys()).thenAnswer(invocation -> Stream.of("qoop:app:cluster:node:local-node"));

        JobExecution initialExecution = dynamicJobLauncher.runDynamicJob("registeredTestJob");
        JobExecution execution = awaitJobCompletion(initialExecution);

        assertNotNull(execution);
        assertEquals(BatchStatus.COMPLETED, execution.getStatus());
    }
}