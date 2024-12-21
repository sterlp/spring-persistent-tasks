package org.sterl.spring.persistent_tasks.trigger.model;

import java.io.Serializable;
import java.time.OffsetDateTime;

import org.apache.commons.lang3.exception.ExceptionUtils;
import org.sterl.spring.persistent_tasks.api.TaskId;
import org.sterl.spring.persistent_tasks.api.TriggerKey;
import org.sterl.spring.persistent_tasks.shared.model.TriggerData;
import org.sterl.spring.persistent_tasks.shared.model.TriggerStatus;

import jakarta.persistence.Column;
import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
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
        @Index(name = "UNQ_SPT_TRIGGERS_KEY", columnList = "trigger_id, task_name", unique = true),
        @Index(name = "IDX_SPT_TRIGGERS_PRIORITY", columnList = "priority"),
        @Index(name = "IDX_SPT_TRIGGERS_RUN_AT", columnList = "run_at"),
        @Index(name = "IDX_SPT_TRIGGERS_STATUS", columnList = "status"),
})
@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(of = "id")
@Builder
public class TriggerEntity {

    @GeneratedValue(strategy = GenerationType.SEQUENCE)
    @Column(updatable = false)
    @Id
    private Long id;

    @Default
    @Embedded
    private TriggerData data = new TriggerData();

    private String runningOn;
    
    public TriggerEntity(TriggerKey key) {
        if (this.data == null) this.data = new TriggerData();
        this.data.setKey(key);
    }

    public TaskId<Serializable> newTaskId() {
        if (data == null || data.getKey() == null) return null;
        return data.getKey().toTaskId();
    }
    
    public TriggerKey getKey() {
        if (data == null) return null;
        return data.getKey();
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

    public boolean isRunning() {
        return data.getStatus() == TriggerStatus.RUNNING;
    }
}
