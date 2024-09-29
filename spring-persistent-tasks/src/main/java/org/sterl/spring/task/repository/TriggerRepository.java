package org.sterl.spring.task.repository;

import java.time.OffsetDateTime;
import java.util.List;

import org.hibernate.LockOptions;
import org.hibernate.cfg.AvailableSettings;
import org.hibernate.jpa.SpecHints;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.jpa.repository.QueryHints;
import org.springframework.data.repository.query.Param;
import org.sterl.spring.task.api.TriggerId;
import org.sterl.spring.task.model.TriggerEntity;
import org.sterl.spring.task.model.TriggerStatus;
import org.sterl.spring.task.model.TaskSchedulerEntity.TaskSchedulerStatus;

import jakarta.persistence.LockModeType;
import jakarta.persistence.QueryHint;

public interface TriggerRepository extends JpaRepository<TriggerEntity, TriggerId> {

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    // https://jakarta.ee/specifications/persistence/3.0/jakarta-persistence-spec-3.0.html#a2132
    @QueryHints({
        @QueryHint(name = AvailableSettings.JAKARTA_LOCK_TIMEOUT, value = "" + LockOptions.SKIP_LOCKED),
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
    
    int countByDataStatus(TriggerStatus status);
    
    @Query("SELECT count(1) FROM #{#entityName} e WHERE e.id.name = :name")
    int countByTriggerName(@Param("name") String name);

    @Query("""
           SELECT e FROM #{#entityName} e
           WHERE e.data.start <= :timeout
           AND e.data.status = :status
           AND (e.runningOn.status <> :schedulerStatus OR e.runningOn.lastPing <= :timeout)
           """)
    List<TriggerEntity> findByTimeout(
            @Param("timeout") OffsetDateTime timeout, 
            @Param("status") TriggerStatus status, 
            @Param("schedulerStatus") TaskSchedulerStatus schedulerStatus);
}
