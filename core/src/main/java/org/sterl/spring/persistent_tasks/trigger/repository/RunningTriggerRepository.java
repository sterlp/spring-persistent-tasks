package org.sterl.spring.persistent_tasks.trigger.repository;

import java.time.OffsetDateTime;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

import org.hibernate.LockOptions;
import org.hibernate.jpa.SpecHints;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.jpa.repository.QueryHints;
import org.springframework.data.repository.query.Param;
import org.sterl.spring.persistent_tasks.api.TriggerKey;
import org.sterl.spring.persistent_tasks.api.TriggerStatus;
import org.sterl.spring.persistent_tasks.shared.repository.TriggerRepository;
import org.sterl.spring.persistent_tasks.trigger.model.RunningTriggerEntity;

import jakarta.persistence.LockModeType;
import jakarta.persistence.QueryHint;

public interface RunningTriggerRepository extends TriggerRepository<RunningTriggerEntity> {

    @Query("SELECT e FROM #{#entityName} e WHERE e.data.key = :key")
    Optional<RunningTriggerEntity> findByKey(@Param("key") TriggerKey key);

    // https://jakarta.ee/specifications/persistence/3.0/jakarta-persistence-spec-3.0.html#a2132
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @QueryHints({
        @QueryHint(name = SpecHints.HINT_SPEC_LOCK_TIMEOUT, value = "" + LockOptions.SKIP_LOCKED),
        @QueryHint(name = SpecHints.HINT_SPEC_QUERY_TIMEOUT, value = "4500")
    })
    @Query("""
           SELECT   e FROM #{#entityName} e
           WHERE    e.data.runAt <= :runAt
           AND      e.data.status = :status
           ORDER BY e.data.priority DESC, e.data.executionCount ASC
           """)
    List<RunningTriggerEntity> loadNextTasks(
            @Param("runAt") OffsetDateTime runAt,
            @Param("status") TriggerStatus status,
            Pageable page);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @QueryHints({
        @QueryHint(name = SpecHints.HINT_SPEC_LOCK_TIMEOUT, value = "" + LockOptions.SKIP_LOCKED),
        @QueryHint(name = SpecHints.HINT_SPEC_QUERY_TIMEOUT, value = "4500")
    })
    @Query("""
           SELECT e FROM #{#entityName} e
           WHERE  e.data.key = :key
           """)
    RunningTriggerEntity lockByKey(@Param("key") TriggerKey key);
    
    @Query("""
            UPDATE RunningTriggerEntity
            SET lastPing = :lastPing, runningOn = :runningOn, data.status = :status
            WHERE data.key IN ( :keys )
            """)
    @Modifying
    int markTriggersAsRunning(
            @Param("keys") Collection<TriggerKey> keys,
            @Param("runningOn") String runningOn,
            @Param("lastPing") OffsetDateTime lastPing,
            @Param("status") TriggerStatus status
        );

    @Query("""
            SELECT e FROM #{#entityName} e
            WHERE  e.data.status = :status
            AND    e.lastPing < :lastPing
            """)
    List<RunningTriggerEntity> findTriggersLastPingAfter(
            @Param("status") TriggerStatus status,
            @Param("lastPing") OffsetDateTime lastPing);
    
    @Query("""
            SELECT e FROM #{#entityName} e
            WHERE  e.data.status = :status
            AND    e.data.runAt <= :runAt
            """)
    List<RunningTriggerEntity> findByStatusAndRunAtAfter(
            @Param("status") TriggerStatus status,
            @Param("runAt") OffsetDateTime runAt,
            Pageable page);
}
