package io.qoop.property.api;

import java.lang.annotation.*;

/**
 * Author: davood akbari
 * Email: daak1365@gmail.com
 * Created: 12/7/2025 4:59 PM
 * Package: io.qoop.infrastructure.model
 */

@Target({ElementType.FIELD, ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface Property {
    String key();

    String defaultValue() default "";
}
