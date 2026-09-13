package io.qoop.outbox.persistence.repository;


import io.qoop.outbox.persistence.entity.ErrorMessageEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface ErrorMessageJpaRepository extends JpaRepository<ErrorMessageEntity, UUID> {
}