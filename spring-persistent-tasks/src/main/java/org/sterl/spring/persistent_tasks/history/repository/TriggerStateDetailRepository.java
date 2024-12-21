package org.sterl.spring.persistent_tasks.history.repository;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.sterl.spring.persistent_tasks.api.HistoryOverview;
import org.sterl.spring.persistent_tasks.api.TriggerId;
import org.sterl.spring.persistent_tasks.history.model.TriggerStateDetailEntity;
import org.sterl.spring.persistent_tasks.shared.model.TriggerStatus;
import org.sterl.spring.persistent_tasks.trigger.model.TriggerEntity;

public interface TriggerStateDetailRepository extends JpaRepository<TriggerStateDetailEntity, Long> {

    @Query("SELECT e FROM #{#entityName} e WHERE e.data.key = :key")
    Optional<TriggerEntity> findByKey(@Param("key") TriggerId key);

    @Query("""
           SELECT e FROM #{#entityName} e
           WHERE  e.data.key.taskName = :taskName
           """)
    Page<TriggerEntity> findAll(
            @Param("taskName") String taskName, Pageable page);
    
    long countByDataStatusIn(Set<TriggerStatus> status);

    @Query("SELECT count(1) FROM #{#entityName} e WHERE e.data.key.taskName = :taskName")
    long countByTaskName(@Param("taskName") String taskName);

    
    @Query("""
            SELECT
                new org.sterl.spring.persistent_tasks.api.HistoryOverview(
                    e.instanceId,
                    e.data.key.taskName,
                    count(1) as entryCount,
                    MIN(e.data.start) as start,
                    MAX(e.data.end) as end,
                    MIN(e.data.createdTime) as createdTime,
                    MAX(e.data.executionCount) as executionCount,
                    AVG(e.data.runningDurationInMs) as runningDurationInMs
                )
            FROM #{#entityName} e
            GROUP BY 
                e.instanceId,
                e.data.key.taskName
            ORDER BY e.data.createdTime DESC
            """)
    Page<HistoryOverview> listHistoryOverview(Pageable page);

    @Query("""
            SELECT e FROM #{#entityName} e
            WHERE e.data.key = :key
            """)
    List<TriggerStateDetailEntity> findByTriggerId(
            @Param("key") TriggerId key,
            Pageable page);
    
    @Query("""
            DELETE FROM #{#entityName} e
            WHERE e.data.createdTime < :age
            """)
    @Modifying
    long deleteHistoryOlderThan(@Param("age") OffsetDateTime age);

    @Query("""
            SELECT COUNT(DISTINCT e.data.key) 
            FROM #{#entityName} e
            WHERE e.data.status = :status
            """)
    long countTriggers(@Param("status") TriggerStatus status);

    @Query("""
            SELECT e
            FROM #{#entityName} e
            WHERE e.instanceId = :instanceId
            ORDER BY e.id ASC
            """)
    List<TriggerStateDetailEntity> findAllByInstanceId(@Param("instanceId") long instanceId);
}
