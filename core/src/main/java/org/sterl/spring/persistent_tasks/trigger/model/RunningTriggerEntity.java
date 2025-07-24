package org.sterl.spring.persistent_tasks.trigger.model;

import java.time.OffsetDateTime;

import org.apache.commons.lang3.exception.ExceptionUtils;
import org.sterl.spring.persistent_tasks.api.TriggerKey;
import org.sterl.spring.persistent_tasks.api.TriggerStatus;
import org.sterl.spring.persistent_tasks.shared.model.HasTrigger;
import org.sterl.spring.persistent_tasks.shared.model.TriggerEntity;

import jakarta.annotation.Nullable;
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
import lombok.ToString;

@Entity
@Table(name = "pt_running_triggers", indexes = {
        @Index(name = "unq_pt_running_triggers_key", columnList = "trigger_id, task_name", unique = true),
        @Index(name = "idx_pt_running_triggers_priority", columnList = "priority"),
        @Index(name = "idx_pt_running_triggers_run_at", columnList = "run_at"),
        @Index(name = "idx_pt_running_triggers_status", columnList = "status"),
        @Index(name = "idx_pt_running_triggers_last_ping", columnList = "last_ping"),
        @Index(name = "idx_pt_running_triggers_correlation_id", columnList = "correlation_id"),
        @Index(name = "idx_pt_running_triggers_tag", columnList = "tag"),
})
@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(of = "id")
@Builder(toBuilder = true)
@ToString(of = {"id", "runningOn", "data"})
public class RunningTriggerEntity implements HasTrigger {

    @GeneratedValue(generator = "seq_pt_task_triggers", strategy = GenerationType.SEQUENCE)
    @Column(updatable = false)
    @Id
    private Long id;

    @Default
    @Embedded
    private TriggerEntity data = new TriggerEntity();

    @Column(length = 200)
    private String runningOn;

    @Nullable
    private OffsetDateTime lastPing;

    public RunningTriggerEntity(TriggerKey key) {
        if (this.data == null) this.data = new TriggerEntity();
        this.data.setKey(key);
    }

    public TriggerKey getKey() {
        if (data == null) return null;
        return data.getKey();
    }
    
    /**
     * @param e Sets either {@link TriggerStatus#SUCCESS} or {@link TriggerStatus#FAILED}
     * based if the {@link Exception} is <code>null</code> or not.
     */
    public RunningTriggerEntity complete(Exception e) {
        finishTriggerWithStatus(e == null ? TriggerStatus.SUCCESS : TriggerStatus.FAILED, e);
        return this;
    }

    public RunningTriggerEntity cancel(Exception e) {
        finishTriggerWithStatus(TriggerStatus.CANCELED, e);
        if (e == null) this.data.setExceptionName("PersistentTask canceled");
        return this;
    }

    public void finishTriggerWithStatus(TriggerStatus status, Exception e) {
        this.data.setEnd(OffsetDateTime.now());
        this.data.updateRunningDuration();
        this.data.setStatus(status);

        if (e != null) {
            this.data.setExceptionName(e.getClass().getName());
            this.data.setLastException(ExceptionUtils.getStackTrace(e));
        }
    }

    public RunningTriggerEntity runOn(String runningOn) {
        this.data.setStart(OffsetDateTime.now());
        this.data.setEnd(null);
        this.data.setExecutionCount(data.getExecutionCount() + 1);
        this.data.setStatus(TriggerStatus.RUNNING);
        this.data.updateRunningDuration();
        this.lastPing = OffsetDateTime.now();
        this.runningOn = runningOn;
        return this;
    }

    public RunningTriggerEntity runAt(OffsetDateTime runAt) {
        data.setStatus(TriggerStatus.WAITING);
        data.setRunAt(runAt);
        setRunningOn(null);
        return this;
    }

    public RunningTriggerEntity withState(byte[] state) {
        this.data.setState(state);
        return this;
    }
    
    public boolean isWaiting() {
        return data.getStatus() == TriggerStatus.WAITING;
    }
    
    public TriggerEntity copyData() {
        if (data == null) return null;
        return this.data.copy();
    }
}
