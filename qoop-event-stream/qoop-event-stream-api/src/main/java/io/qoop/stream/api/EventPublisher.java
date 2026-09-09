package io.qoop.stream.api;

import java.util.List;

/**
 * Generic Event Publisher interface.
 * K: type of the key
 * V: type of the event payload
 */
public interface EventPublisher {

    void publish(String key, Object payload);

    void publish(Object payload);

    void publish(List<Header> headers, String key, Object payload);

    void publish(String topic, List<Header> headers, String key, Object payload);

    void publish(String topic, String key, Object payload);

    void publishWithTopic(String topic, Object payload);

    void publish(List<Header> headers, Object payload);

    void publish(String topic, List<Header> headers, Object payload);
}
