package org.sterl.spring.task.model;

import java.io.Serializable;
import java.time.OffsetDateTime;

import org.apache.commons.lang3.exception.ExceptionUtils;
import org.sterl.spring.task.api.TaskId;

import jakarta.persistence.Column;
import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.Index;
import jakarta.persistence.Lob;
import jakarta.persistence.ManyToOne;
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
        @Index(name = "IDX_TASK_TRIGGERS_START", columnList = "start_time"),
        @Index(name = "IDX_TASK_TRIGGERS_STATUS", columnList = "status"),
})
@Data
@NoArgsConstructor
@Builder(toBuilder = true)
@AllArgsConstructor
@ToString(of = {"id", "status", "created", "executionCount", "priority", "start", "end"})
@EqualsAndHashCode(of = "id")
public class TriggerEntity {

    @EmbeddedId
    private TriggerId id;
    
    @ManyToOne(cascade = {}, fetch = FetchType.LAZY, optional = true)
    private TaskSchedulerEntity runningOn;

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
    private TriggerStatus status = TriggerStatus.NEW;

    @Lob
    private byte[] state;
    
    @Column(length = 512)
    private String exceptionName;
    @Lob
    private String lastException;

    public TriggerEntity cancel() {
        end = OffsetDateTime.now();
        status = TriggerStatus.CANCELED;
        exceptionName = "Task canceled";
        return this;
    }

    public void runOn(TaskSchedulerEntity runningOn) {
        this.start = OffsetDateTime.now();
        this.end = null;
        this.executionCount += 1;
        this.runningOn = runningOn;
        this.status = TriggerStatus.OPEN;
    }
    public void complete(TriggerStatus newStatus, Exception e) {
        fail(e);
        this.status = newStatus;
    }
    public void fail(Exception e) {
        this.end = OffsetDateTime.now();
        this.status = TriggerStatus.FAILED;
        this.exceptionName = e == null ? "" : e.getClass().getName();
        this.lastException = ExceptionUtils.getStackTrace(e);
    }
}
