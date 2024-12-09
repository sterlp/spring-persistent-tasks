package org.sterl.spring.persistent_tasks.history.repository;

import java.time.OffsetDateTime;
import java.util.List;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.sterl.spring.persistent_tasks.api.TriggerId;
import org.sterl.spring.persistent_tasks.history.model.TriggerHistoryEntity;
import org.sterl.spring.persistent_tasks.shared.model.TriggerStatus;

public interface TriggerHistoryRepository extends JpaRepository<TriggerHistoryEntity, Long> {

    @Query("""
            SELECT e FROM #{#entityName} e
            WHERE e.triggerId = :triggerId
            """)
    List<TriggerHistoryEntity> findByTriggerId(
            @Param("triggerId") TriggerId triggerId,
            Pageable page);
    
    @Query("""
            DELETE FROM #{#entityName} e
            WHERE e.createDate <= :age
            """)
    @Modifying
    long deleteHistoryOlderThan(@Param("age") OffsetDateTime age);

    @Query("""
            SELECT COUNT(DISTINCT triggerId.id) 
            FROM #{#entityName} e
            WHERE e.data.status = :status
            """)
    long countTriggers(@Param("status") TriggerStatus status);
}
