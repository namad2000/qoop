package io.qoop.mapper.api.mapper;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

public interface SourceMapper<S, T> extends TargetMapper<S, T> {

    // Converts a single Target object back to a Source object
    // Example: UserDto -> User, User -> CreateUserCommand
    S toSource(T t);

    @SuppressWarnings("OptionalUsedAsFieldOrParameterType")
    default Optional<S> toSource(Optional<T> optionalTarget) {
        return optionalTarget.map(this::toSource);
    }

    // Converts a List of Target objects to a List of Source objects
    default List<S> toSource(List<T> tList) {
        return tList.stream().map(this::toSource).collect(Collectors.toList());
    }

    // Converts a Set of Target objects to a Set of Source objects
    default Set<S> toSource(Set<T> tSet) {
        return tSet.stream().map(this::toSource).collect(Collectors.toSet());
    }
}
