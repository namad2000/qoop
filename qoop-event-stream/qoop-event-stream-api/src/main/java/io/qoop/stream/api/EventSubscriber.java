package io.qoop.stream.api;

public interface EventSubscriber<V> {
    void onMessage(V event);
}