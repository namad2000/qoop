package io.qoop.mapper.api.mapper;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

public interface TargetMapper<S, T> {

    // Converts a single Source object to a Target object
    // Example: User -> UserDto, CreateUserCommand -> User
    T toTarget(S s);

    @SuppressWarnings("OptionalUsedAsFieldOrParameterType")
    default Optional<T> toTarget(Optional<S> optionalSource) {
        return optionalSource.map(this::toTarget);
    }

    // Converts a List of Source objects to a List of Target objects
    default List<T> toTarget(List<S> sList) {
        return sList.stream().map(this::toTarget).collect(Collectors.toList());
    }

    // Converts a Set of Source objects to a Set of Target objects
    default Set<T> toTarget(Set<S> sSet) {
        return sSet.stream().map(this::toTarget).collect(Collectors.toSet());
    }
}
