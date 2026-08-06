package io.qoop.domain.repository;

import java.util.Optional;

/**
 * Author: davood akbari
 * Email: daak1365@gmail.com
 * Created: 12/15/2025 10:08 PM
 * Package: io.qoop.domain.repository
 */

public interface FindRepository<D, ID> {
    Optional<D> findById(ID id);

    boolean existsById(ID id);
}
