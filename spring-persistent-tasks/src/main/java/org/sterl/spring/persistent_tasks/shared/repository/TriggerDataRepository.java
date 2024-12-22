package org.sterl.spring.persistent_tasks.shared.repository;

import java.time.OffsetDateTime;
import java.util.Set;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.NoRepositoryBean;
import org.springframework.data.repository.query.Param;
import org.sterl.spring.persistent_tasks.shared.model.HasTriggerData;
import org.sterl.spring.persistent_tasks.shared.model.TriggerStatus;
import org.sterl.spring.persistent_tasks.trigger.model.TriggerEntity;

@NoRepositoryBean
public interface TriggerDataRepository<T extends HasTriggerData> extends JpaRepository<T, Long> {

    @Query("""
           SELECT e FROM #{#entityName} e
           WHERE  e.data.key.taskName = :taskName
           """)
    Page<TriggerEntity> findAll(
            @Param("taskName") String taskName, Pageable page);
    
    @Query("""
           SELECT COUNT(DISTINCT e.data.key) 
           FROM #{#entityName} e WHERE e.data.key.taskName = :taskName
           """)
    long countByTaskName(@Param("taskName") String taskName);
    
    @Query("""
           SELECT COUNT(DISTINCT e.data.key) 
           FROM #{#entityName} e
           WHERE e.data.status = :status
           """)
    long countByStatus(@Param("status") TriggerStatus status);

    @Query("""
            SELECT COUNT(DISTINCT e.data.key) 
            FROM #{#entityName} e
            WHERE e.data.status IN ( :status )
            """)
     long countByStatus(@Param("status") Set<TriggerStatus> status);
    
    @Query("""
           DELETE FROM #{#entityName} e
           WHERE e.data.createdTime < :age
           """)
    @Modifying
    long deleteOlderThan(@Param("age") OffsetDateTime age);
}
