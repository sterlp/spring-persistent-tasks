package org.sterl.spring.persistent_tasks.scheduler.entity;

import java.time.OffsetDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Entity
@Table(name = "SPT_SCHEDULER",
       indexes = @Index(name = "IDX_TASK_SCHEDULER_STATUS", columnList = "status"))
@Data
@ToString(of = {"id", "status", "runnungTasks", "tasksSlotCount"})
@EqualsAndHashCode(of = "id")
@NoArgsConstructor
public class SchedulerEntity {
    public enum TaskSchedulerStatus {
        ONLINE,
        OFFLINE
    }

    /**
     * The unique name of the scheduler, each one should have an own e.g. host + port
     */
    @Id
    @Column(updatable = false)
    private String id;

    private int tasksSlotCount;

    private int runnungTasks;

    private double systemLoadAverage = 0.0d;

    private long maxHeap;

    private long usedHeap;

    private TaskSchedulerStatus status = TaskSchedulerStatus.ONLINE;

    private OffsetDateTime lastPing = OffsetDateTime.now();

    public SchedulerEntity(String name) {
        super();
        this.id = name;
    }
}
