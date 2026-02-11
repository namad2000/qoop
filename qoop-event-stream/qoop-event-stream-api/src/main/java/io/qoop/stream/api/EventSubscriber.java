package ir.online.commons.stream.api;

public interface EventSubscriber<V> {
    void onMessage(V event);
}