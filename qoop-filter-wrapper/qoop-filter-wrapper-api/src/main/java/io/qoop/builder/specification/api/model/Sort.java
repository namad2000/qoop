package io.qoop.builder.specification.api.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Sort {

    public enum Direction {
        ASC("asc"), DESC("desc");

        private final String name;

        Direction(String name) {
            this.name = name;
        }

        public String getName() {
            return name;
        }

        public static Direction fromString(String value) {
            return getValue(value);
        }

        public static Direction getValue(String value) {
            for (Direction direction : Direction.values()) {
                if (direction.name.equalsIgnoreCase(value)) {
                    return direction;
                }
            }
            return null;
        }
    }

    private Direction direction;
    private String property;
}