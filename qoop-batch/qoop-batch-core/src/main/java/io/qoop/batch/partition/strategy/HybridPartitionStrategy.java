package io.qoop.batch.partition.strategy;

import io.qoop.batch.config.BatchKafkaProperties;
import io.qoop.cluster.ClusterNodeCounter;
import io.qoop.cluster.ClusterRoleDetector;
import io.qoop.cluster.NodeIdentity;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.partition.Partitioner;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.Step;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.infrastructure.item.ExecutionContext;
import org.springframework.batch.integration.partition.MessageChannelPartitionHandler;
import org.springframework.integration.core.MessagingTemplate;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.PollableChannel;
import org.springframework.transaction.PlatformTransactionManager;

import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

@Slf4j
public class HybridPartitionStrategy implements PartitionStrategy {

    private final NodeIdentity nodeIdentity;
    private final ClusterNodeCounter nodeCounter;
    private final ClusterRoleDetector roleDetector;
    private final BatchKafkaProperties batchKafkaProperties;
    private final int partitionCount;
    private final int chunkSize;
    private final boolean kafkaEnabled;

    private final MessageChannel outboundRequests;
    private final PollableChannel inboundReplies;

    public HybridPartitionStrategy(
            NodeIdentity nodeIdentity,
            ClusterNodeCounter nodeCounter,
            ClusterRoleDetector roleDetector,
            BatchKafkaProperties batchKafkaProperties,
            int partitionCount,
            int chunkSize,
            boolean kafkaEnabled,
            MessageChannel outboundRequests,
            PollableChannel inboundReplies) {
        this.nodeIdentity = nodeIdentity;
        this.nodeCounter = nodeCounter;
        this.roleDetector = roleDetector;
        this.batchKafkaProperties = batchKafkaProperties;
        this.partitionCount = partitionCount;
        this.chunkSize = chunkSize;
        this.kafkaEnabled = kafkaEnabled;
        this.outboundRequests = outboundRequests;
        this.inboundReplies = inboundReplies;
    }

    @Override
    public Partitioner createDefaultPartitioner() {
        return gridSize -> {
            var partitions = new ConcurrentHashMap<String, ExecutionContext>();
            List<String> aliveNodes = getWorkerNodes();
            String selfId = nodeIdentity.getNodeId();

            int partitionIndex = 0;
            for (int i = 0; i < gridSize; i++) {
                String assignedNode = aliveNodes.isEmpty() ? selfId : aliveNodes.get(i % aliveNodes.size());
                boolean isLocal = assignedNode.equals(selfId);

                var context = new ExecutionContext();
                context.put("partitionId", i);
                context.put("nodeId", assignedNode);
                context.put("isLocal", isLocal);
                context.put("chunkSize", chunkSize);
                context.put("startIndex", partitionIndex * chunkSize);
                context.put("endIndex", (partitionIndex + 1) * chunkSize - 1);
                context.put("timestamp", System.currentTimeMillis());

                partitions.put("partition-" + i, context);
                partitionIndex++;
            }
            return partitions;
        };
    }

    @Override
    public Step createPartitionStepWithPartitioner(
            String stepName,
            JobRepository jobRepository,
            PlatformTransactionManager transactionManager,
            Step workerStep,
            Partitioner partitioner) {

        log.info("Creating HYBRID partition step: {} with {} partitions (Namespace: {})",
                stepName, getPartitionCount(), batchKafkaProperties.getKafka().getNamespace());

        String workerStepBeanName = workerStep.getName();

        if (!isMaster()) {
            log.info("Node {} acting as Worker for step: {} [Namespace: {}]",
                    nodeIdentity.getNodeId(), stepName, batchKafkaProperties.getKafka().getNamespace());
            return workerStep;
        }

        MessageChannelPartitionHandler partitionHandler = new MessageChannelPartitionHandler();
        partitionHandler.setStepName(workerStepBeanName);
        partitionHandler.setGridSize(getPartitionCount());

        MessagingTemplate messagingTemplate = new MessagingTemplate(outboundRequests);
        messagingTemplate.setReceiveTimeout(60000);

        partitionHandler.setMessagingOperations(messagingTemplate);
        partitionHandler.setReplyChannel(inboundReplies);

        try {
            partitionHandler.afterPropertiesSet();
        } catch (Exception e) {
            log.error("Failed to initialize MessageChannelPartitionHandler for step: {}", stepName, e);
        }

        return new StepBuilder(stepName, jobRepository)
                .partitioner(workerStepBeanName, partitioner)
                .partitionHandler(partitionHandler)
                .build();
    }

    @Override
    public boolean isMaster() {
        try {
            String masterKey = batchKafkaProperties.getKafka().getResolvedMasterRoleKey();
            return roleDetector.isMaster(masterKey);
        } catch (Exception e) {
            log.warn("Failed to check master status, assuming worker: {}", e.getMessage());
            return false;
        }
    }

    public List<String> getWorkerNodes() {
        try {
            List<String> nodeKeys = nodeCounter.scanNodeKeys()
                    .collect(Collectors.toList());

            if (nodeKeys.isEmpty()) {
                return List.of(nodeIdentity.getNodeId());
            }

            return nodeKeys;
        } catch (Exception e) {
            log.warn("Failed to scan node keys, falling back to local node: {}", e.getMessage());
            return List.of(nodeIdentity.getNodeId());
        }
    }

    @Override
    public int getPartitionCount() {
        List<String> nodes = getWorkerNodes();
        int nodeCount = Math.max(1, nodes.size());
        return Math.max(partitionCount, nodeCount * 2);
    }

    @Override
    public int getChunkSize() {
        return chunkSize;
    }

    @Override
    public boolean isKafkaEnabled() {
        return kafkaEnabled;
    }
}