package io.qoop.stream.api.annotaions;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotation that defines an Event in DDD style.
 * The 'value' represents the event name or channel.
 */

@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface Event {
    String value(); // Event name or channel

    Header[] headers() default {}; // Associated metadata and headers of the message
}
