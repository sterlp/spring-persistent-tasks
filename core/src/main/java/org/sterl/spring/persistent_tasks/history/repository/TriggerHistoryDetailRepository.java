package org.sterl.spring.persistent_tasks.history.repository;

import java.util.List;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.sterl.spring.persistent_tasks.history.model.TriggerHistoryDetailEntity;

public interface TriggerHistoryDetailRepository extends HistoryTriggerRepository<TriggerHistoryDetailEntity> {

     @Query("""
             SELECT e
             FROM #{#entityName} e
             WHERE e.instanceId = :instanceId
             ORDER BY e.id DESC
             """)
     List<TriggerHistoryDetailEntity> findAllByInstanceId(@Param("instanceId") long instanceId);
}
