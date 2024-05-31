package org.sterl.spring.task.model;

import java.io.Serializable;
import java.time.OffsetDateTime;
import java.util.function.IntPredicate;

import org.apache.commons.lang3.exception.ExceptionUtils;
import org.sterl.spring.task.api.TaskId;

import jakarta.persistence.Column;
import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
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

@Entity
@Table(name = "TASK_TRIGGERS", indexes = {
        @Index(name = "IDX_TASK_TRIGGERS_PRIORITY", columnList = "priority"),
        @Index(name = "IDX_TASK_TRIGGERS_START", columnList = "start_time")
})
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString(of = {"id", "status", "created", "priority", "start", "end"})
@EqualsAndHashCode(of = "id")
public class TaskTriggerEntity {

    @EmbeddedId
    private TaskTriggerId id;

    public TaskId<Serializable> newTaskId() {
        return id.toTaskId();
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
    
    @Column(length = 512)
    private String exceptionName;
    @Lob
    private String lastException;

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
        fail(e);
        this.status = newStatus;
    }
    public void fail(Exception e) {
        this.end = OffsetDateTime.now();
        this.status = TaskStatus.FAILED;
        this.exceptionName = e == null ? "" : e.getClass().getName();
        this.lastException = ExceptionUtils.getStackTrace(e);
    }
}
