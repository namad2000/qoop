package io.qoop.global.outbox;

import io.qoop.outbox.OutboxEventPublisher;
import io.qoop.stream.api.Header;

import java.util.List;

public class NoOpOutboxEventPublisher implements OutboxEventPublisher {

    public static final OutboxEventPublisher INSTANCE = new NoOpOutboxEventPublisher();

    @Override
    public void publish(List<Header> headers, Object payload) {
        // Intentional No-Op
    }

    @Override
    public void publish(Object payload) {
        // Intentional No-Op
    }
}
