package io.qoop.mapper.api.mapstruct;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

public interface BasicMapper<S, T> {

    // Converts a single Source object to a Target object
    // Example: User -> UserDto, CreateUserCommand -> User
    T toTarget(S s);

    // Converts a single Target object back to a Source object
    // Example: UserDto -> User, User -> CreateUserCommand
    S toSource(T t);


    @SuppressWarnings("OptionalUsedAsFieldOrParameterType")
    default Optional<T> toTarget(Optional<S> optionalSource) {
        return optionalSource.map(this::toTarget);
    }

    @SuppressWarnings("OptionalUsedAsFieldOrParameterType")
    default Optional<S> toSource(Optional<T> optionalTarget) {
        return optionalTarget.map(this::toSource);
    }

    // Converts a List of Source objects to a List of Target objects
    default List<T> toTarget(List<S> sList) {
        return sList.stream().map(this::toTarget).collect(Collectors.toList());
    }

    // Converts a List of Target objects to a List of Source objects
    default List<S> toSource(List<T> tList) {
        return tList.stream().map(this::toSource).collect(Collectors.toList());
    }

    // Converts a Set of Source objects to a Set of Target objects
    default Set<T> toTarget(Set<S> sSet) {
        return sSet.stream().map(this::toTarget).collect(Collectors.toSet());
    }

    // Converts a Set of Target objects to a Set of Source objects
    default Set<S> toSource(Set<T> tSet) {
        return tSet.stream().map(this::toSource).collect(Collectors.toSet());
    }
}
