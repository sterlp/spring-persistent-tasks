package org.sterl.spring.persistent_tasks.scheduler.repository;

import java.time.OffsetDateTime;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.sterl.spring.persistent_tasks.scheduler.entity.SchedulerEntity;
import org.sterl.spring.persistent_tasks.scheduler.entity.SchedulerEntity.TaskSchedulerStatus;

public interface TaskSchedulerRepository extends JpaRepository<SchedulerEntity, String>{

    @Query("""
            UPDATE SchedulerEntity
            SET status = :status
            WHERE status != :status
            AND lastPing < :timeout
            """)
    @Modifying
    int setSchedulersStatusByLastPing(
            @Param("timeout") OffsetDateTime timeout, 
            @Param("status") TaskSchedulerStatus status);

}
