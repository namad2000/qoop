package io.qoop.domain.repository;

import java.util.Optional;

/**
 * Author: davood akbari
 * Email: daak1365@gmail.com
 * Created: 8/16/2026 7:48 AM
 */

public interface BaseInformationRepository<D, C> {
    boolean existsByCode(C code);

    Optional<D> findByCode(C code);
}
