package ir.online.commons.stream.api;

/**
 * Generic Event Publisher interface.
 * K: type of the key
 * V: type of the event payload
 */
public interface EventPublisher {

    void publish(String key, Object payload);

    void publish(Object payload);
}
