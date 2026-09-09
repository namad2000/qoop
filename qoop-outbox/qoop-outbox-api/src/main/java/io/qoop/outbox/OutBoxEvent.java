package io.qoop.outbox;

import io.qoop.stream.api.annotaions.Header;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;


@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface OutBoxEvent {
    String channel(); // Event name or channel or topic

    String aggregateType(); // Type of the aggregate or entity generating the event (e.g., Order)

    String eventType(); // Type of event or name of the action performed (e.g., OrderCreated)

    Header[] headers() default {}; // Associated metadata and headers of the message
}
