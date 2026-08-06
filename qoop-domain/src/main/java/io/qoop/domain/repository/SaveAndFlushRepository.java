package io.qoop.domain.repository;

import java.util.List;

/**
 * Author: davood akbari
 * Email: daak1365@gmail.com
 * Created: 12/15/2025 10:08 PM
 * Package: io.qoop.domain.repository
 */

public interface SaveAndFlushRepository<D, ID> {
    void flush();

    D saveAndFlush(D domain);

    List<D> saveAllAndFlush(Iterable<D> domains);
}
