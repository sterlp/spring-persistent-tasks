package org.sterl.spring.persistent_tasks.shared.repository;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Set;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.domain.Sort.Direction;
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
    Sort DEFAULT_SORT = Sort.by(Direction.ASC, "data.createdTime");

    default Pageable applyDefaultSortIfNeeded(Pageable page) {
        var result = page;
        if (page.getSort() == Sort.unsorted()) {
            result = PageRequest.of(page.getPageNumber(), page.getPageSize(), DEFAULT_SORT);
        }
        return result;
    }

    @Query("""
            SELECT e FROM #{#entityName} e
            WHERE ((:id IS NULL       OR e.data.key.id LIKE :id)
                OR (:id IS NULL       OR e.data.correlationId LIKE :id))
            AND    (:taskName IS NULL OR e.data.key.taskName = :taskName)
            AND    (:status IS NULL   OR e.data.status = :status)
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
            SELECT   e FROM #{#entityName} e
            WHERE    e.data.correlationId = :correlationId
            """)
    List<T> findByCorrelationId(@Param("correlationId") String correlationId, Pageable page);
}
