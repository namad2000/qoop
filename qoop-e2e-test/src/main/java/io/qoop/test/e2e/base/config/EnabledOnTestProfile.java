package io.qoop.test.e2e.base.config;

import org.junit.jupiter.api.condition.EnabledIfSystemProperty;

import java.lang.annotation.*;

@Target({ElementType.TYPE, ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Inherited
@EnabledIfSystemProperty(named = "spring.profiles.active", matches = "test")
public @interface EnabledOnTestProfile {
}