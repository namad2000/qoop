package io.qoop.outbox;

import io.qoop.stream.api.Header;

import java.util.List;

public interface OutboxEventPublisher {

    void publish(List<Header> headers, Object payload);

    void publish(Object payload);
}
