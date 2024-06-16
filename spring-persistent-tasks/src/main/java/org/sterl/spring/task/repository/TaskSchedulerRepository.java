package org.sterl.spring.task.repository;

import java.time.OffsetDateTime;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.sterl.spring.task.model.TaskSchedulerEntity;
import org.sterl.spring.task.model.TaskSchedulerEntity.TaskSchedulerStatus;

public interface TaskSchedulerRepository extends JpaRepository<TaskSchedulerEntity, String>{

    @Query("""
            UPDATE #{#entityName} e
            SET e.status = :status
            WHERE e.status != :status
            AND e.lastPing < :timeout
            """)
    @Modifying
    int setSchedulersStatusByLastPing(
            @Param("timeout") OffsetDateTime timeout, 
            @Param("status")TaskSchedulerStatus status);

}
