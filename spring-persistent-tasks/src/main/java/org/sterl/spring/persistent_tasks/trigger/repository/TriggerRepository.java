package org.sterl.spring.persistent_tasks.trigger.repository;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import org.hibernate.LockOptions;
import org.hibernate.jpa.SpecHints;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.jpa.repository.QueryHints;
import org.springframework.data.repository.query.Param;
import org.sterl.spring.persistent_tasks.api.TriggerKey;
import org.sterl.spring.persistent_tasks.shared.model.TriggerStatus;
import org.sterl.spring.persistent_tasks.shared.repository.TriggerDataRepository;
import org.sterl.spring.persistent_tasks.trigger.model.TriggerEntity;

import jakarta.persistence.LockModeType;
import jakarta.persistence.QueryHint;

public interface TriggerRepository extends TriggerDataRepository<TriggerEntity> {
    @Query("SELECT e FROM #{#entityName} e WHERE e.data.key = :key")
    Optional<TriggerEntity> findByKey(@Param("key") TriggerKey key);

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
    List<TriggerEntity> loadNextTasks(
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
    TriggerEntity lockByKey(@Param("key") TriggerKey key);

    @Query("""
           SELECT e FROM #{#entityName} e
           WHERE  e.runningOn NOT IN ( :runningOn )
           AND    e.data.status = :status
           """)
    List<TriggerEntity> findNotRunningOn(
            @Param("runningOn") Set<String> runningOn,
            @Param("status") TriggerStatus status);
}
