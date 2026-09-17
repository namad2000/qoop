package io.qoop.batch.config;

import lombok.RequiredArgsConstructor;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.integration.partition.BeanFactoryStepLocator;
import org.springframework.batch.integration.partition.StepExecutionRequestHandler;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.integration.dsl.IntegrationFlow;

@Configuration
@RequiredArgsConstructor
public class BatchWorkerConfiguration {

    private final BatchPayloadConverter payloadConverter;

    @Bean
    public StepExecutionRequestHandler stepExecutionRequestHandler(
            JobRepository jobRepository,
            ApplicationContext applicationContext) {

        StepExecutionRequestHandler handler = new StepExecutionRequestHandler();
        handler.setJobRepository(jobRepository);

        BeanFactoryStepLocator stepLocator = new BeanFactoryStepLocator();
        stepLocator.setBeanFactory(applicationContext);
        handler.setStepLocator(stepLocator);

        return handler;
    }

    @Bean
    public IntegrationFlow inboundRequestsWorkerFlow(StepExecutionRequestHandler handler) {
        return IntegrationFlow
                .from("${batch.kafka.namespace:default}-inboundRequests")
                .transform(payloadConverter::convertToStepExecutionIfNeeded)
                .handle(handler)
                .channel("${batch.kafka.namespace:default}-outboundReplies")
                .get();
    }
}