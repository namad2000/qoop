package io.qoop.mapper.api;

import lombok.Getter;

@Getter
class UserEntity {
    private Long id;
    private String name;

    // constructor
    public UserEntity(Long id, String name) {
        this.id = id;
        this.name = name;
    }
}
