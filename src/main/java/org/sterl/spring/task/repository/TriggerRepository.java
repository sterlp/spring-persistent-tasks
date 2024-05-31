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
import org.sterl.spring.task.model.TriggerEntity;
import org.sterl.spring.task.model.TriggerId;

import jakarta.persistence.LockModeType;
import jakarta.persistence.QueryHint;

import org.sterl.spring.task.model.TriggerStatus;

public interface TriggerRepository extends JpaRepository<TriggerEntity, TriggerId> {

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    // https://jakarta.ee/specifications/persistence/3.0/jakarta-persistence-spec-3.0.html#a2132
    @QueryHints({
        @QueryHint(name = AvailableSettings.JAKARTA_LOCK_TIMEOUT, value = "" + LockOptions.SKIP_LOCKED),
        @QueryHint(name = SpecHints.HINT_SPEC_QUERY_TIMEOUT, value = "4500")
    })
    @Query("""
            SELECT e FROM #{#entityName} e
            WHERE start <= :start
            AND status = :status
            ORDER BY priority DESC, executionCount ASC
            """)
    List<TriggerEntity> loadNextTasks(OffsetDateTime start, TriggerStatus status, Pageable page);
    
    int countByStatus(TriggerStatus status);
}
