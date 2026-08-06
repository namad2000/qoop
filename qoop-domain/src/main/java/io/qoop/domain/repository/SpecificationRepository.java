package io.qoop.domain.repository;

import io.qoop.builder.specification.api.model.FilterWrapper;
import io.qoop.builder.specification.api.model.SortWrapper;
import io.qoop.domain.model.PageFilterData;

/**
 * Author: davood akbari
 * Email: daak1365@gmail.com
 * Created: 12/15/2025 10:08 PM
 * Package: io.qoop.domain.repository
 */

public interface SpecificationRepository<D> {
    PageFilterData<D> findAll(FilterWrapper filterWrapper,
                              SortWrapper sortWrapper,
                              Integer start,
                              Integer limit);
}
