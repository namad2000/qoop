package io.qoop.jpa.persistence.mapper;

/**
 * Author: davood akbari
 * Email: daak1365@gmail.com
 * Created: 12/15/2025 10:18 PM
 * Package: io.qoop.infrastructure.persistence.mapper
 */

public interface InfrastructureMapper<D, E> {

    D toDomain(E e);

    E toEntity(D d);
}
