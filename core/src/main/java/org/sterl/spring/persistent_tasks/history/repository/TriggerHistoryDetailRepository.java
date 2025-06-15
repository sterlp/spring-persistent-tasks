package org.sterl.spring.persistent_tasks.history.repository;

import java.util.List;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.sterl.spring.persistent_tasks.history.model.HistoryTriggerEntity;

public interface TriggerHistoryDetailRepository extends HistoryTriggerRepository<HistoryTriggerEntity> {

     @Query("""
             SELECT e
             FROM #{#entityName} e
             WHERE e.instanceId = :instanceId
             ORDER BY e.id DESC
             """)
     List<HistoryTriggerEntity> findAllByInstanceId(@Param("instanceId") long instanceId);
}
