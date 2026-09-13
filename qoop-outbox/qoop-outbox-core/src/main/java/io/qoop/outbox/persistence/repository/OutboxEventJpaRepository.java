package io.qoop.outbox.persistence.repository;

import io.qoop.outbox.persistence.entity.OutboxEventEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Repository
public interface OutboxEventJpaRepository extends JpaRepository<OutboxEventEntity, UUID> {

    @Query(value = """
            SELECT * FROM outbox_event
            WHERE status = 'NEW'
              AND MOD(ABS(ORA_HASH(TO_CHAR(id))), :gridSize) = :partitionIndex
            ORDER BY created_at
            FETCH FIRST :limit ROWS ONLY
            FOR UPDATE SKIP LOCKED
            """, nativeQuery = true)
    List<OutboxEventEntity> findNewForPartition(@Param("partitionIndex") int partitionIndex,
                                                @Param("gridSize") int gridSize,
                                                @Param("limit") int limit);

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("UPDATE OutboxEventEntity e SET e.status = 'SENT', e.sentAt = :sentAt, e.version = e.version + 1 WHERE e.id = :id AND e.version = :version")
    int markAsSent(@Param("id") UUID id, @Param("sentAt") LocalDateTime sentAt, @Param("version") Long version);

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("UPDATE OutboxEventEntity e SET e.status = 'FAILED', e.retryCount = e.retryCount + 1, e.version = e.version + 1 WHERE e.id = :id AND e.version = :version")
    int markAsFailed(@Param("id") UUID id, @Param("version") Long version);

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query(value = "DELETE FROM outbox_event WHERE status = 'SENT' AND sent_at < :threshold", nativeQuery = true)
    void deleteSentBefore(@Param("threshold") LocalDateTime threshold);

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query(value = "DELETE FROM outbox_event WHERE created_at < :threshold", nativeQuery = true)
    void deleteCreatedBefore(@Param("threshold") LocalDateTime threshold);
}