package io.qoop.domain.repository;

import io.qoop.domain.model.PageData;

import java.util.Optional;

/**
 * Author: davood akbari
 * Email: daak1365@gmail.com
 * Created: 12/15/2025 10:08 PM
 * Package: io.qoop.domain.repository
 */

public interface DomainRepository<D, ID> {
    D save(D domain);

    Optional<D> findById(ID id);

    Boolean existById(ID id);

    PageData<D> findAll(Integer pageNumber, Integer pageSize);

    void delete(D domain, boolean logical);

    void delete(D domain);
}
