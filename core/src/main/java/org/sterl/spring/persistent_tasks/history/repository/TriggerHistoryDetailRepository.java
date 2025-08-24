package org.sterl.spring.persistent_tasks.history.repository;

import java.time.OffsetDateTime;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.querydsl.QuerydslPredicateExecutor;
import org.springframework.data.repository.query.Param;
import org.sterl.spring.persistent_tasks.api.TriggerStatus;
import org.sterl.spring.persistent_tasks.history.model.HistoryTriggerEntity;

public interface TriggerHistoryDetailRepository
        extends JpaRepository<HistoryTriggerEntity, Long>, QuerydslPredicateExecutor<HistoryTriggerEntity> {

    @Query("""
            SELECT e
            FROM #{#entityName} e
            WHERE e.instanceId = :instanceId
            """)
    Page<HistoryTriggerEntity> findAllByInstanceId(
            @Param("instanceId") long instanceId, Pageable page);

    @Query("""
            DELETE FROM #{#entityName} e
            WHERE e.createdTime < :age
            """)
    @Modifying
    long deleteOlderThan(@Param("age") OffsetDateTime age);

    @Query("""
            SELECT COUNT(e.id)
            FROM #{#entityName} e
            WHERE e.status = :status
            """)
    long countByStatus(@Param("status") TriggerStatus status);
}
