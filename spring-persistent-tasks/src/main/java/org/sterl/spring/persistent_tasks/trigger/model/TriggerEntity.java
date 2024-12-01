package org.sterl.spring.persistent_tasks.trigger.model;

import java.io.Serializable;
import java.time.OffsetDateTime;

import org.apache.commons.lang3.exception.ExceptionUtils;
import org.sterl.spring.persistent_tasks.api.TaskId;
import org.sterl.spring.persistent_tasks.api.TriggerId;

import jakarta.persistence.Embedded;
import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.Index;
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

    private String runningOn;

    public TaskId<Serializable> newTaskId() {
        return id.toTaskId();
    }

    public TriggerEntity cancel() {
        data.setEnd(OffsetDateTime.now());
        data.setStatus(TriggerStatus.CANCELED);
        data.setExceptionName("Task canceled");
        return this;
    }

    public TriggerEntity runOn(String runningOn) {
        data.setStart(OffsetDateTime.now());
        data.setEnd(null);
        data.setExecutionCount(data.getExecutionCount() + 1);
        data.setStatus(TriggerStatus.RUNNING);
        this.runningOn = runningOn;
        return this;
    }

    public void complete(Exception e) {
        data.setStatus(TriggerStatus.SUCCESS);
        data.setEnd(OffsetDateTime.now());

        if (e != null) {
            data.setStatus(TriggerStatus.FAILED);
            data.setExceptionName(e.getClass().getName());
            data.setLastException(ExceptionUtils.getStackTrace(e));
        }
    }

    public TriggerEntity runAt(OffsetDateTime triggerTime) {
        data.setStatus(TriggerStatus.NEW);
        data.setTriggerTime(triggerTime);
        return this;
    }
}
