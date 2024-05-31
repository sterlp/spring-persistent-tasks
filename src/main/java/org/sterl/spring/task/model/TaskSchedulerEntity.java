package org.sterl.spring.task.model;

import java.time.OffsetDateTime;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "TASK_SCHEDULER_STATUS")
@Data
@EqualsAndHashCode(of = "name")
@NoArgsConstructor
public class TaskSchedulerEntity {
    public enum TaskSchedulerStatus {
        ONLINE,
        OFFLINE
    }

    @Id
    private String name;
    
    private int tasksSlotCount;
    
    private int runnungTasks;
    
    private double systemLoadAverage;
    
    private long maxHeap;
    
    private long usedHeap;
    
    private TaskSchedulerStatus status = TaskSchedulerStatus.ONLINE;
    
    private OffsetDateTime lastPing = OffsetDateTime.now();

    public TaskSchedulerEntity(String name) {
        super();
        this.name = name;
    }
}
