package io.qoop.mapper.api;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public interface InputMapper<C, D> {

    D toDomain(C input, MappingContext context);

    default D toDomain(C input) {
        return toDomain(input, MappingContext.empty());
    }

    default List<D> toDomain(List<C> inputs, MappingContext context) {
        if (inputs == null) return List.of();
        return inputs.stream()
                .map(c -> toDomain(c, context))
                .collect(Collectors.toList());
    }

    default Set<D> toDomain(Set<C> inputs, MappingContext context) {
        if (inputs == null) return Set.of();
        return inputs.stream()
                .map(c -> toDomain(c, context))
                .collect(Collectors.toCollection(LinkedHashSet::new));
    }

    default List<D> toDomain(List<C> inputs) {
        return toDomain(inputs, MappingContext.empty());
    }

    default Set<D> toDomain(Set<C> inputs) {
        return toDomain(inputs, MappingContext.empty());
    }
}

