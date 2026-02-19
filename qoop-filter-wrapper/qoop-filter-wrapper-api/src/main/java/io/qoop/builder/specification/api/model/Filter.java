package io.qoop.builder.specification.api.model;


import lombok.*;

@Data
@ToString
@EqualsAndHashCode
@AllArgsConstructor
@NoArgsConstructor
public class Filter {

    public enum Operator {
        _EQUAL("equal"),
        EQUAL("eq"),
        NOT_EQUAL("neq"),
        LIKE("like"),
        BETWEEN("between"),
        AFTER("after"),
        BEFORE("before"),
        IN("in"),
        NOT_IN("nin"),
        GREATER_THAN_OR_EQUAL("gte"),
        LESS_THAN_OR_EQUAL("lte"),
        GREATER_THAN("gt"),
        LESS_THAN("lt"),
        IS_NULL("isn"),
        IS_NOT_NULL("inn"),
        MAX("max"),
        LIKE_CASE_INSENSITIVE("lci");

        private final String name;

        Operator(String name) {
            this.name = name;
        }

        public String getName() {
            return name;
        }

        public static Operator fromString(String value) {
            for (Operator op : Operator.values()) {
                if (op.name.equalsIgnoreCase(value) || op.name().equalsIgnoreCase(value))
                    return op;
            }
            return null;
        }
    }

    private String property;
    private String value;
    private Operator operator;
}