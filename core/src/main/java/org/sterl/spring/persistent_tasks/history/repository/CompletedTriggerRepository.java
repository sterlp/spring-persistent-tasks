package org.sterl.spring.persistent_tasks.history.repository;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.sterl.spring.persistent_tasks.api.TaskStatusHistoryOverview;
import org.sterl.spring.persistent_tasks.api.TriggerKey;
import org.sterl.spring.persistent_tasks.history.model.CompletedTriggerEntity;
import org.sterl.spring.persistent_tasks.shared.repository.TriggerRepository;

public interface CompletedTriggerRepository extends TriggerRepository<CompletedTriggerEntity> {

    @Query("""
           SELECT new org.sterl.spring.persistent_tasks.api.TaskStatusHistoryOverview(
             e.data.key.taskName,
             e.data.status,
             count(1),
             MIN(e.data.runAt) as firstRun,
             MAX(e.data.runAt) as lastRun,
             MAX(e.data.runningDurationInMs) as maxDuration,
             MIN(e.data.runningDurationInMs) as minDuration,
             AVG(e.data.runningDurationInMs) as avgDuration,
             AVG(e.data.executionCount) as avgExecutionCount
           )
           FROM #{#entityName} e
           GROUP BY e.data.key.taskName, e.data.status
           ORDER BY e.data.key.taskName ASC, e.data.status ASC
           """)
    List<TaskStatusHistoryOverview> listTriggerStatus();
    
    @Query("""
            SELECT e FROM #{#entityName} e 
            WHERE e.data.key = :key
            """)
    Page<CompletedTriggerEntity> listKnownStatusFor(@Param("key") TriggerKey key, Pageable page);
}
