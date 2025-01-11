package org.sterl.spring.persistent_tasks.history.repository;

import java.util.List;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.sterl.spring.persistent_tasks.api.TaskHistoryOverview;
import org.sterl.spring.persistent_tasks.history.model.TriggerHistoryDetailEntity;

public interface TriggerHistoryDetailRepository extends HistoryTriggerRepository<TriggerHistoryDetailEntity> {

    @Query("""
            SELECT new org.sterl.spring.persistent_tasks.api.TaskHistoryOverview(
                     e.data.key.taskName,
                     count(1) as entryCount,
                     MIN(e.data.runAt) as firstRun,
                     MAX(e.data.runAt) as lastRun,
                     MAX(e.data.runningDurationInMs) as maxDuration,
                     MIN(e.data.runningDurationInMs) as minDuration,
                     AVG(e.data.runningDurationInMs) as avgDuration
                   )
             FROM #{#entityName} e
             WHERE e.data.end IS NOT NULL
             GROUP BY e.data.key.taskName
             ORDER BY e.data.key.taskName ASC
             """)
     List<TaskHistoryOverview> listTaskHistoryOverview();

     @Query("""
             SELECT e
             FROM #{#entityName} e
             WHERE e.instanceId = :instanceId
             ORDER BY e.id DESC
             """)
     List<TriggerHistoryDetailEntity> findAllByInstanceId(@Param("instanceId") long instanceId);
}
