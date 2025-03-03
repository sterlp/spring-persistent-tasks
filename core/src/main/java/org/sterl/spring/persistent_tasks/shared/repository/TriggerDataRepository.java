package org.sterl.spring.persistent_tasks.shared.repository;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Set;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.NoRepositoryBean;
import org.springframework.data.repository.query.Param;
import org.sterl.spring.persistent_tasks.api.TriggerKey;
import org.sterl.spring.persistent_tasks.api.TriggerStatus;
import org.sterl.spring.persistent_tasks.shared.model.HasTriggerData;

@NoRepositoryBean
public interface TriggerDataRepository<T extends HasTriggerData> extends JpaRepository<T, Long> {
    @Query("""
            SELECT e FROM #{#entityName} e
            WHERE  (e.data.key.id LIKE :id% OR :id IS NULL)
            AND    (e.data.key.taskName = :taskName OR :taskName IS NULL)
            AND    (e.data.status = :status OR :status IS NULL)
            """)
     Page<T> findAll(@Param("id") String id,
             @Param("taskName") String taskName,
             @Param("status") TriggerStatus status,
             Pageable page);

    @Query("""
           SELECT e FROM #{#entityName} e
           WHERE  e.data.key.taskName = :taskName
           """)
    Page<T> findAll(@Param("taskName") String taskName, Pageable page);
    
    @Query("""
           SELECT COUNT(e.data.key) 
           FROM #{#entityName} e WHERE e.data.key.taskName = :taskName
           """)
    long countByTaskName(@Param("taskName") String taskName);

    @Query("""
            SELECT COUNT(e.data.key) 
            FROM #{#entityName} e WHERE e.data.key = :key
            """)
    long countByKey(@Param("key") TriggerKey key);

    @Query("""
           SELECT COUNT(e.id) 
           FROM #{#entityName} e
           WHERE e.data.status = :status
           """)
    long countByStatus(@Param("status") TriggerStatus status);

    @Query("""
            SELECT COUNT(e.id) 
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
    
    @Query("""
            SELECT e FROM #{#entityName} e
            WHERE  e.data.correlationId = :correlationId
            ORDER BY e.data.createdTime ASC
            """)
    List<T> findByCorrelationId(@Param("correlationId") String correlationId);
}
