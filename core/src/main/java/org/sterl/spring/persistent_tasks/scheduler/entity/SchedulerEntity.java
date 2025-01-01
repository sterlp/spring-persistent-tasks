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
@Table(name = "pt_scheduler", indexes = @Index(name = "idx_task_scheduler_status", columnList = "last_ping"))
@Data
@ToString(of = { "id", "lastPing", "runnungTasks", "tasksSlotCount" })
@EqualsAndHashCode(of = "id")
@NoArgsConstructor
public class SchedulerEntity {
    /**
     * The unique name of the scheduler, each one should have an own e.g. host +
     * port
     */
    @Id
    @Column(updatable = false)
    private String id;

    private int tasksSlotCount;

    private int runnungTasks;

    private double systemLoadAverage = 0.0d;

    private long maxHeap;

    private long usedHeap;

    private OffsetDateTime lastPing = OffsetDateTime.now();

    public SchedulerEntity(String name) {
        super();
        this.id = name;
    }
}
