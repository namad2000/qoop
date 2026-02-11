package io.qoop.mapper.api;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public interface ResultMapper<D, R> {

    R toResult(D domain, MappingContext context);

    default R toResult(D domain) {
        return toResult(domain, MappingContext.empty());
    }

    default List<R> toResult(List<D> domains, MappingContext context) {
        if (domains == null) return List.of();
        return domains.stream()
                .map(d -> toResult(d, context))
                .collect(Collectors.toList());
    }

    default Set<R> toResult(Set<D> domains, MappingContext context) {
        if (domains == null) return Set.of();
        return domains.stream()
                .map(d -> toResult(d, context))
                .collect(Collectors.toCollection(LinkedHashSet::new));
    }

    default List<R> toResult(List<D> domains) {
        return toResult(domains, MappingContext.empty());
    }

    default Set<R> toResult(Set<D> domains) {
        return toResult(domains, MappingContext.empty());
    }
}

