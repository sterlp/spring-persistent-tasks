package org.sterl.spring.persistent_tasks.trigger.repository;

import java.util.List;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.sterl.spring.persistent_tasks.api.TriggerId;
import org.sterl.spring.persistent_tasks.trigger.model.TriggerHistoryEntity;

public interface TriggerHistoryRepository extends JpaRepository<TriggerHistoryEntity, Long> {

    @Query("""
            SELECT e FROM #{#entityName} e
            WHERE e.triggerId = :triggerId
            """)
    List<TriggerHistoryEntity> findByTriggerId(
            @Param("triggerId") TriggerId triggerId, 
            Pageable page);
}
