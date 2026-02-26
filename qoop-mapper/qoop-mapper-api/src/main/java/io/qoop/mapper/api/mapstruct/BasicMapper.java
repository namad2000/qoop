package io.qoop.mapper.api.mapstruct;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public interface BasicMapper<SOURCE, TARGET> {

    // Converts a single Source object to a Target object
    // Example: User -> UserDto, CreateUserCommand -> User
    TARGET toTarget(SOURCE source);

    // Converts a single Target object back to a Source object
    // Example: UserDto -> User, User -> CreateUserCommand
    SOURCE toSource(TARGET target);


    @SuppressWarnings("OptionalUsedAsFieldOrParameterType")
    default Optional<TARGET> toTarget(Optional<SOURCE> optionalSource) {
        return optionalSource.map(this::toTarget);
    }

    @SuppressWarnings("OptionalUsedAsFieldOrParameterType")
    default Optional<SOURCE> toSource(Optional<TARGET> optionalTarget) {
        return optionalTarget.map(this::toSource);
    }

    // Converts a List of Source objects to a List of Target objects
    default List<TARGET> toTarget(List<SOURCE> sourceList) {
        return sourceList.stream().map(this::toTarget).collect(Collectors.toList());
    }

    // Converts a List of Target objects to a List of Source objects
    default List<SOURCE> toSource(List<TARGET> targetList) {
        return targetList.stream().map(this::toSource).collect(Collectors.toList());
    }
}
