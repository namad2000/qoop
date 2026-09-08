package io.qoop.cluster;

import lombok.Getter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.net.InetAddress;
import java.util.UUID;

@Getter
@Component
public class NodeIdentity {

    private final String nodeId;

    public NodeIdentity(@Value("${spring.application.name:app}") String appName) {
        this.nodeId = buildNodeId(appName);
    }

    private String buildNodeId(String appName) {
        try {
            String host = InetAddress.getLocalHost().getHostName();
            return appName + "@" + host + "-" + UUID.randomUUID().toString().substring(0, 8);
        } catch (Exception e) {
            return appName + "@" + UUID.randomUUID();
        }
    }
}