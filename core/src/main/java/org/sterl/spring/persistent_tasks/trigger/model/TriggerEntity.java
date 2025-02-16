package org.sterl.spring.persistent_tasks.trigger.model;

import java.time.OffsetDateTime;

import org.apache.commons.lang3.exception.ExceptionUtils;
import org.sterl.spring.persistent_tasks.api.TriggerKey;
import org.sterl.spring.persistent_tasks.api.TriggerStatus;
import org.sterl.spring.persistent_tasks.shared.model.HasTriggerData;
import org.sterl.spring.persistent_tasks.shared.model.TriggerData;

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

@Entity
@Table(name = "pt_task_triggers", indexes = {
        @Index(name = "unq_pt_triggers_key", columnList = "trigger_id, task_name", unique = true),
        @Index(name = "idx_pt_triggers_priority", columnList = "priority"),
        @Index(name = "idx_pt_triggers_run_at", columnList = "run_at"),
        @Index(name = "idx_pt_triggers_status", columnList = "status"),
        @Index(name = "idx_pt_triggers_ping", columnList = "last_ping"),
        @Index(name = "idx_pt_triggers_correlation_id", columnList = "correlation_id"),
})
@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(of = "id")
@Builder
public class TriggerEntity implements HasTriggerData {

    @GeneratedValue(generator = "seq_pt_task_triggers", strategy = GenerationType.SEQUENCE)
    @Column(updatable = false)
    @Id
    private Long id;

    @Default
    @Embedded
    private TriggerData data = new TriggerData();

    @Column(length = 200)
    private String runningOn;

    @Nullable
    private OffsetDateTime lastPing;

    public TriggerEntity(TriggerKey key) {
        if (this.data == null) this.data = new TriggerData();
        this.data.setKey(key);
    }

    public TriggerKey getKey() {
        if (data == null) return null;
        return data.getKey();
    }

    public TriggerEntity cancel() {
        this.data.setEnd(OffsetDateTime.now());
        this.data.setStatus(TriggerStatus.CANCELED);
        this.data.setExceptionName("PersistentTask canceled");
        this.data.setRunningDurationInMs(null);
        return this;
    }

    public TriggerEntity runOn(String runningOn) {
        this.data.setStart(OffsetDateTime.now());
        this.data.setEnd(null);
        this.data.setExecutionCount(data.getExecutionCount() + 1);
        this.data.setStatus(TriggerStatus.RUNNING);
        this.data.updateRunningDuration();
        this.lastPing = OffsetDateTime.now();
        this.runningOn = runningOn;
        return this;
    }

    /**
     * @param e Sets either {@link TriggerStatus#SUCCESS} or {@link TriggerStatus#FAILED}
     * based if the {@link Exception} is <code>null</code> or not.
     */
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

    public TriggerEntity runAt(OffsetDateTime runAt) {
        data.setStatus(TriggerStatus.WAITING);
        data.setRunAt(runAt);
        setRunningOn(null);
        return this;
    }

    public TriggerEntity withState(byte[] state) {
        this.data.setState(state);
        return this;
    }
    
    public TriggerData copyData() {
        if (data == null) return null;
        return this.data.copy();
    }
}
