package io.qoop.batch.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Getter
@Setter
@Component
@ConfigurationProperties(prefix = "batch")
public class BatchKafkaProperties {
    private Kafka kafka = new Kafka();
    private Partition partition = new Partition();
    private TaskExecutor taskExecutor = new TaskExecutor();

    @Getter
    @Setter
    public static class Kafka {
        /**
         * Unique namespace for isolating topics, channels, and cluster roles per job/instance.
         */
        private String namespace = "default";

        private String requestTopic;
        private String replyTopic;
        private String groupId;
        private String masterRoleKey;
        private Boolean enabled = true;

        /**
         * Resolves the request topic by applying the namespace prefix if needed.
         */
        public String getResolvedRequestTopic() {
            if (requestTopic == null || requestTopic.isBlank()) {
                throw new IllegalArgumentException("Property 'batch.kafka.request-topic' must be configured.");
            }
            String prefix = "qoop-batch." + (namespace != null && !namespace.isBlank() ? namespace + "." : "");
            return requestTopic.startsWith("qoop-batch.") ? requestTopic : prefix + requestTopic;
        }

        /**
         * Resolves the reply topic by applying the namespace prefix if needed.
         */
        public String getResolvedReplyTopic() {
            if (replyTopic == null || replyTopic.isBlank()) {
                throw new IllegalArgumentException("Property 'batch.kafka.reply-topic' must be configured.");
            }
            String prefix = "qoop-batch." + (namespace != null && !namespace.isBlank() ? namespace + "." : "");
            return replyTopic.startsWith("qoop-batch.") ? replyTopic : prefix + replyTopic;
        }

        /**
         * Resolves the consumer group ID, appending the namespace to prevent cross-job consumption.
         */
        public String getResolvedGroupId(String defaultBaseGroup) {
            if (groupId != null && !groupId.isBlank()) {
                return groupId;
            }
            String baseGroup = (defaultBaseGroup != null && !defaultBaseGroup.isBlank()) ? defaultBaseGroup : "qoop-batch-group";
            return (namespace != null && !namespace.isBlank()) ? baseGroup + "-" + namespace : baseGroup;
        }

        /**
         * Resolves the master leadership election key based on the namespace.
         */
        public String getResolvedMasterRoleKey() {
            if (masterRoleKey != null && !masterRoleKey.isBlank()) {
                return masterRoleKey;
            }
            return "qoop-batch-master" + (namespace != null && !namespace.isBlank() ? "-" + namespace : "");
        }
    }

    @Getter
    @Setter
    public static class Partition {
        private Strategy strategy = new Strategy();
        private Integer gridSize = 4;
        private Integer chunkSize = 10;

        @Getter
        @Setter
        public static class Strategy {
            private Boolean forceLocal = false;
        }
    }

    @Getter
    @Setter
    public static class TaskExecutor {
        private String type = "qoop-async";
        private String threadNamePrefix = "qoop-batch-";
        private Integer concurrencyLimit = 10;
    }
}