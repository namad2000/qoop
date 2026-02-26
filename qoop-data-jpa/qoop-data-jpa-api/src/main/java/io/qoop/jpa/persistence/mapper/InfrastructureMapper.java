package io.qoop.jpa.persistence.mapper;

import java.util.Optional;

/**
 * Author: davood akbari
 * Email: daak1365@gmail.com
 * Created: 12/15/2025 10:18 PM
 * Package: io.qoop.infrastructure.persistence.mapper
 */

public interface InfrastructureMapper<D, E> {

    D toDomain(E e);

    E toEntity(D d);

    @SuppressWarnings("OptionalUsedAsFieldOrParameterType")
    default Optional<D> toDomain(Optional<E> e) {
        return e.map(this::toDomain);
    }

    @SuppressWarnings("OptionalUsedAsFieldOrParameterType")
    default Optional<E> toEntity(Optional<D> e) {
        return e.map(this::toEntity);
    }
}
