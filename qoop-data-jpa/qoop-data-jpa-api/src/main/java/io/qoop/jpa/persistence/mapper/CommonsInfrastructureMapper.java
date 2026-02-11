package io.qoop.jpa.persistence.mapper;

/**
 * Author: davood akbari
 * Email: daak1365@gmail.com
 * Created: 12/15/2025 10:18 PM
 * Package: ir.online.commons.infrastructure.persistence.mapper
 */

public interface CommonsInfrastructureMapper<D, E> {

    D toDomain(E e);

    E toEntity(D d);
}
