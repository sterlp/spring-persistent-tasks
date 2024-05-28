package org.sterl.spring.task.model;

import java.io.Serializable;
import java.time.OffsetDateTime;

import org.sterl.spring.task.api.TaskId;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.IdClass;
import jakarta.persistence.Index;
import jakarta.persistence.Lob;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Builder.Default;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;

@IdClass(TaskTriggerId.class)
@Entity
@Table(name = "TASK_TRIGGERS", indexes = {
        @Index(name = "IDX_TASK_TRIGGERS_PRIORITY", columnList = "priority"),
        @Index(name = "IDX_TASK_TRIGGERS_START", columnList = "start_time")
})
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString(of = {"id", "name", "taskGroup", "status", "created", "priority", "start", "end"})
@EqualsAndHashCode(of = {"id", "name", "taskGroup"})
public class TaskTriggerEntity {

    @Id
    @Column(nullable = false, updatable = false, length = 50)
    private String id;
    @Id
    @Column(nullable = false, updatable = false, length = 100)
    private String name;
    @Id
    @Column(nullable = false, updatable = false, length = 100)
    private String taskGroup;
    
    public TaskTriggerId newInstanceId() {
        return new TaskTriggerId(id, name, taskGroup);
    }
    public TaskId<Serializable> newId() {
        return new TaskId<Serializable>(name, taskGroup);
    }

    @Default
    @Column(updatable = false, name = "created_time")
    private OffsetDateTime created = OffsetDateTime.now();

    @Column(name = "start_time")
    private OffsetDateTime start;

    @Column(name = "end_time")
    private OffsetDateTime end;

    @Default
    private int executionCount = 0;

    /** priority, the higher a more priority it will get */
    @Default
    private int priority = 4;

    @Enumerated(EnumType.STRING)
    @Column(length = 20, nullable = false)
    @Default
    private TaskStatus status = TaskStatus.NEW;

    private String runningOn;

    @Lob
    private byte[] state;
    /*
    @Lob
    private byte[] lastError;
    */

    public TaskTriggerEntity cancel() {
        end = OffsetDateTime.now();
        status = TaskStatus.CANCELED;
        return this;
    }

    public void runOn(String runningOn) {
        this.start = OffsetDateTime.now();
        this.executionCount += 1;
        this.runningOn = runningOn;
        this.status = TaskStatus.RUNNING;
    }
    public void complete(TaskStatus newStatus, Exception e) {
        this.end = OffsetDateTime.now();
        this.status = newStatus;
    }
}
