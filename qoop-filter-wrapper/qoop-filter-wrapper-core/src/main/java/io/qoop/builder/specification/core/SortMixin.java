package io.qoop.builder.specification.core;

import com.fasterxml.jackson.annotation.JsonCreator;
import io.qoop.builder.specification.api.model.Sort;

public abstract class SortMixin {

    @JsonCreator
    public static Sort.Direction fromString(String value) {
        if (value == null) {
            return null;
        }
        for (Sort.Direction direction : Sort.Direction.values()) {
            if (direction.name().equalsIgnoreCase(value)) {
                return direction;
            }
            if (direction.getName().equalsIgnoreCase(value)) {
                return direction;
            }
        }
        return null;
    }
}