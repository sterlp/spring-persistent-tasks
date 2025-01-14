package org.sterl.spring.persistent_tasks.scheduler.repository;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Set;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.sterl.spring.persistent_tasks.scheduler.entity.SchedulerEntity;

public interface TaskSchedulerRepository extends JpaRepository<SchedulerEntity, String>{

    @Query("""
           DELETE FROM #{#entityName}
           WHERE lastPing < :timeout
           """)
    @Modifying
    int deleteOldSchedulers(@Param("timeout") OffsetDateTime timeout);
    
    @Query("""
           SELECT e.id FROM #{#entityName} e
           ORDER BY e.id
           """)
    Set<String> findSchedulerNames();

    @Query("""
           SELECT e FROM #{#entityName} e
           ORDER BY e.id
           """)
    List<SchedulerEntity> listAll();
}
