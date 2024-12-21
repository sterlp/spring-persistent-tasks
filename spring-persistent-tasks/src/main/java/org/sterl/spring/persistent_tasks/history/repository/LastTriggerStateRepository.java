package org.sterl.spring.persistent_tasks.history.repository;

import java.util.Optional;
import java.util.Set;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.sterl.spring.persistent_tasks.api.TriggerId;
import org.sterl.spring.persistent_tasks.history.model.LastTriggerStateEntity;
import org.sterl.spring.persistent_tasks.shared.model.TriggerStatus;
import org.sterl.spring.persistent_tasks.trigger.model.TriggerEntity;

public interface LastTriggerStateRepository extends JpaRepository<LastTriggerStateEntity, Long> {

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
}
