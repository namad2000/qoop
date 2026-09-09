package io.qoop.outbox.persistence.repository;


import io.qoop.outbox.persistence.entity.ErrorMessageEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface ErrorMessageJpaRepository extends JpaRepository<ErrorMessageEntity, UUID> {
}