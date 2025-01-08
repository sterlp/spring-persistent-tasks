package org.sterl.spring.persistent_tasks.history.repository;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.sterl.spring.persistent_tasks.api.HistoryOverview;
import org.sterl.spring.persistent_tasks.history.model.TriggerHistoryDetailEntity;

public interface TriggerHistoryDetailRepository extends HistoryTriggerRepository<TriggerHistoryDetailEntity> {

    @Query("""
            SELECT new org.sterl.spring.persistent_tasks.api.HistoryOverview(
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
             ORDER BY end DESC, createdTime DESC
             """)
     Page<HistoryOverview> listHistoryOverview(Pageable page);

     @Query("""
             SELECT e
             FROM #{#entityName} e
             WHERE e.instanceId = :instanceId
             ORDER BY e.id DESC
             """)
     List<TriggerHistoryDetailEntity> findAllByInstanceId(@Param("instanceId") long instanceId);
}
