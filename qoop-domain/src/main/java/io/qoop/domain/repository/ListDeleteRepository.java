package io.qoop.domain.repository;

/**
 * Author: davood akbari
 * Email: daak1365@gmail.com
 * Created: 12/15/2025 10:08 PM
 * Package: io.qoop.domain.repository
 */

public interface ListDeleteRepository<D, ID> {
    void deleteAllById(Iterable<? extends ID> ids);

    void deleteAll(Iterable<? extends D> domains);

    void deleteAll();
}
