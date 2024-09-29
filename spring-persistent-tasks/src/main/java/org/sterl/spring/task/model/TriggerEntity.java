package org.sterl.spring.task.model;

import java.io.Serializable;
import java.time.OffsetDateTime;

import org.apache.commons.lang3.exception.ExceptionUtils;
import org.sterl.spring.task.api.TaskId;
import org.sterl.spring.task.api.TriggerId;

import jakarta.persistence.Embedded;
import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Index;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "TASK_TRIGGERS", indexes = {
        @Index(name = "IDX_TASK_TRIGGERS_PRIORITY", columnList = "priority"),
        @Index(name = "IDX_TASK_TRIGGERS_TIME", columnList = "trigger_time"),
        @Index(name = "IDX_TASK_TRIGGERS_STATUS", columnList = "status"),
})
@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(of = "id")
public class TriggerEntity {

    @EmbeddedId
    private TriggerId id;
    
    @Embedded
    private BaseTriggerData data = new BaseTriggerData();

    @ManyToOne(cascade = {}, fetch = FetchType.LAZY, optional = true)
    private TaskSchedulerEntity runningOn;

    public TaskId<Serializable> newTaskId() {
        return id.toTaskId();
    }

    public TriggerEntity cancel() {
        data.setEnd(OffsetDateTime.now());
        data.setStatus(TriggerStatus.CANCELED);
        data.setExceptionName("Task canceled");
        return this;
    }

    public void runOn(TaskSchedulerEntity runningOn) {
        data.setStart(OffsetDateTime.now());
        data.setEnd(null);
        data.setExecutionCount(data.getExecutionCount() + 1);
        data.setStatus(TriggerStatus.RUNNING);
        this.runningOn = runningOn;
    }
    public void complete(TriggerStatus newStatus, Exception e) {
        fail(e);
        data.setStatus(newStatus);
    }
    public void fail(Exception e) {
        data.setEnd(OffsetDateTime.now());
        data.setStatus(TriggerStatus.FAILED);
        data.setExceptionName(e == null ? "" : e.getClass().getName());
        data.setLastException(ExceptionUtils.getStackTrace(e));
    }
    
    public void runAt(OffsetDateTime start) {
        data.setStatus(TriggerStatus.NEW);
        data.setTriggerTime(start);
    }
}
