package org.sterl.spring.persistent_tasks.trigger.repository;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Set;

import org.hibernate.LockOptions;
import org.hibernate.jpa.SpecHints;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.jpa.repository.QueryHints;
import org.springframework.data.repository.query.Param;
import org.sterl.spring.persistent_tasks.api.TriggerId;
import org.sterl.spring.persistent_tasks.trigger.model.TriggerEntity;
import org.sterl.spring.persistent_tasks.trigger.model.TriggerStatus;

import jakarta.persistence.LockModeType;
import jakarta.persistence.QueryHint;

public interface TriggerRepository extends JpaRepository<TriggerEntity, TriggerId> {

    // https://jakarta.ee/specifications/persistence/3.0/jakarta-persistence-spec-3.0.html#a2132
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @QueryHints({
        @QueryHint(name = SpecHints.HINT_SPEC_LOCK_TIMEOUT, value = "" + LockOptions.SKIP_LOCKED),
        @QueryHint(name = SpecHints.HINT_SPEC_QUERY_TIMEOUT, value = "4500")
    })
    @Query("""
            SELECT e FROM #{#entityName} e
            WHERE data.triggerTime <= :triggerTime
            AND data.status = :status
            ORDER BY data.priority DESC, data.executionCount ASC
            """)
    List<TriggerEntity> loadNextTasks(
            @Param("triggerTime") OffsetDateTime triggerTime, 
            @Param("status") TriggerStatus status, 
            Pageable page);
    
    long countByDataStatus(TriggerStatus status);

    @Query("SELECT count(1) FROM #{#entityName} e WHERE e.id.name = :name")
    long countByTriggerName(@Param("name") String name);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @QueryHints({
        @QueryHint(name = SpecHints.HINT_SPEC_LOCK_TIMEOUT, value = "" + LockOptions.SKIP_LOCKED),
        @QueryHint(name = SpecHints.HINT_SPEC_QUERY_TIMEOUT, value = "4500")
    })
    @Query("""
            SELECT e FROM #{#entityName} e
            WHERE e.id <= :id
            """)
    TriggerEntity lockById(@Param("id") TriggerId id);

    @Query("""
            SELECT e FROM #{#entityName} e
            WHERE e.runningOn NOT IN ( :runningOn )
            AND e.data.status = :status
            """)
    List<TriggerEntity> findNotRunningOn(
            @Param("runningOn") Set<String> runningOn,
            @Param("status") TriggerStatus status);
}
