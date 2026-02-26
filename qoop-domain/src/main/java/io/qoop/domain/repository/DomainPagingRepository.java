package io.qoop.domain.repository;

import io.qoop.domain.model.PageData;

/**
 * Author: davood akbari
 * Email: daak1365@gmail.com
 * Created: 12/15/2025 10:08 PM
 * Package: io.qoop.domain.repository
 */

public interface DomainPagingRepository<D> {

    PageData<D> findAll(Integer pageNumber, Integer pageSize);
}
