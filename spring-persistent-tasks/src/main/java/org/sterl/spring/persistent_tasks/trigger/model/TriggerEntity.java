package org.sterl.spring.persistent_tasks.trigger.model;

import java.io.Serializable;
import java.time.OffsetDateTime;

import org.apache.commons.lang3.exception.ExceptionUtils;
import org.sterl.spring.persistent_tasks.api.TaskId;
import org.sterl.spring.persistent_tasks.api.TriggerId;
import org.sterl.spring.persistent_tasks.shared.model.TriggerData;
import org.sterl.spring.persistent_tasks.shared.model.TriggerStatus;

import jakarta.persistence.Embedded;
import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Builder.Default;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "SPT_TASK_TRIGGERS", indexes = {
        @Index(name = "IDX_TASK_TRIGGERS_PRIORITY", columnList = "priority"),
        @Index(name = "IDX_TASK_TRIGGERS_RUN_AT", columnList = "run_at"),
        @Index(name = "IDX_TASK_TRIGGERS_STATUS", columnList = "status"),
})
@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(of = "id")
@Builder
public class TriggerEntity {

    @EmbeddedId
    private TriggerId id;

    @Default
    @Embedded
    private TriggerData data = new TriggerData();

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

    public TriggerEntity complete(Exception e) {
        data.setStatus(TriggerStatus.SUCCESS);
        data.setEnd(OffsetDateTime.now());
        data.updateRunningDuration();
        if (e != null) {
            data.setStatus(TriggerStatus.FAILED);
            data.setExceptionName(e.getClass().getName());
            data.setLastException(ExceptionUtils.getStackTrace(e));
        }

        return this;
    }

    public TriggerEntity failWithMessage(String message) {
        this.data.setStatus(TriggerStatus.FAILED);
        this.data.setExceptionName(message);
        this.data.setLastException(null);
        return this;
    }

    public TriggerEntity runAt(OffsetDateTime runAt) {
        data.setStatus(TriggerStatus.NEW);
        data.setRunAt(runAt);
        return this;
    }
}
