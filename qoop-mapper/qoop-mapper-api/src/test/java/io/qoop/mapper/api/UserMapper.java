package io.qoop.mapper.api;

import java.util.Objects;

public class UserMapper implements InputMapper<UserEntity, UserDomain>, ResultMapper<UserDomain, UserDto> {

    @Override
    public UserDomain toDomain(UserEntity input, MappingContext context) {
        Objects.requireNonNull(input, "input must not be null");

        return new UserDomain(
                input.getId(),
                input.getName()
        );
    }

    @Override
    public UserDto toResult(UserDomain domain, MappingContext context) {
        Objects.requireNonNull(domain, "domain must not be null");

        boolean upper = Boolean.TRUE.equals(context.get("upper"));
        String name = domain.getName();

        UserDto dto = new UserDto();
        dto.setId(domain.getId());
        dto.setFullName(upper ? name.toUpperCase() : name);
        return dto;
    }
}

