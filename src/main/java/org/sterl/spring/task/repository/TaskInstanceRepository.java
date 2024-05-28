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
import org.sterl.spring.task.model.TaskTriggerEntity;
import org.sterl.spring.task.model.TaskTriggerId;

import jakarta.persistence.LockModeType;
import jakarta.persistence.QueryHint;

import org.sterl.spring.task.model.TaskStatus;

public interface TaskInstanceRepository extends JpaRepository<TaskTriggerEntity, TaskTriggerId> {

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
    List<TaskTriggerEntity> loadNextTasks(OffsetDateTime start, TaskStatus status, Pageable page);
    
    int countByStatus(TaskStatus status);
}
