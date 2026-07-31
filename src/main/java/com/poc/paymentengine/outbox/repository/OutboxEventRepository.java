package com.poc.paymentengine.outbox.repository;


import com.poc.paymentengine.outbox.entity.OutboxEvent;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;

@Repository
public interface OutboxEventRepository extends JpaRepository<OutboxEvent, Long> {

    @Modifying
    @Transactional
    @Query(value = """
        UPDATE outbox_events
        SET status = 'PROCESSING',
            locked_at = NOW()
        WHERE id IN
        (
            SELECT id
            FROM outbox_events
            WHERE status = 'PENDING'
            AND next_retry_at <= NOW()
            ORDER BY id
            LIMIT :limit
            FOR UPDATE SKIP LOCKED
        )
      """, nativeQuery = true)
    int claimPendingEvents(int limit);

    @Query(value = """
        SELECT *
        FROM outbox_events
        WHERE status = 'PROCESSING'
        ORDER BY id
        LIMIT :limit
        """,
            nativeQuery = true)
    List<OutboxEvent> findProcessingEvents(int limit);

    @Modifying
    @Transactional
    @Query(value = """
        UPDATE outbox_events
        SET status = 'PENDING',
            locked_at = NULL
        WHERE status = 'PROCESSING'
        AND locked_at < NOW() - INTERVAL '10 minutes'
        """,
            nativeQuery = true)
    int releaseStuckEvents();
}