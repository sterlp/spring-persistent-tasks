package org.sterl.spring.persistent_tasks.history.repository;

import java.util.List;

import org.springframework.data.jpa.repository.Query;
import org.sterl.spring.persistent_tasks.api.TaskStatusHistoryOverview;
import org.sterl.spring.persistent_tasks.history.model.TriggerHistoryLastStateEntity;

public interface TriggerHistoryLastStateRepository extends HistoryTriggerRepository<TriggerHistoryLastStateEntity> {

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
}
