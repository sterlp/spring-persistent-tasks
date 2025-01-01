package org.sterl.spring.persistent_tasks.history.model;

import java.time.OffsetDateTime;

import org.sterl.spring.persistent_tasks.api.TriggerKey;
import org.sterl.spring.persistent_tasks.shared.model.HasTriggerData;
import org.sterl.spring.persistent_tasks.shared.model.TriggerData;

import jakarta.persistence.Column;
import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

/**
 * Just a copy of the trigger status but without any data/state.
 */
@Entity
@Table(name = "pt_trigger_history_details", indexes = {
        @Index(name = "idx_pt_triggers_history_instance_id", columnList = "instance_id"),
        @Index(name = "idx_pt_triggers_history_task_name", columnList = "task_name"),
        @Index(name = "idx_pt_triggers_history_trigger_id", columnList = "trigger_id"),
        @Index(name = "idx_pt_triggers_history_status", columnList = "status"),
        @Index(name = "idx_pt_triggers_history_created_time", columnList = "created_time"),
})
@Data
@NoArgsConstructor
@Builder(toBuilder = true)
@AllArgsConstructor
@EqualsAndHashCode(of = "id")
public class TriggerHistoryDetailEntity implements HasTriggerData {

    @GeneratedValue(generator = "seq_pt_trigger_history_details", strategy = GenerationType.SEQUENCE)
    @Column(updatable = false)
    @Id
    private Long id;

    /**
     * The original ID of this trigger in case grouping is needed
     * as for each trigger multiple history entries are added.
     */
    private Long instanceId;

    @Embedded
    @NotNull
    private TriggerData data;

    public TriggerKey getKey() {
        return data.getKey();
    }

    public void setCreatedTime(OffsetDateTime time) {
        this.data.setCreatedTime(time);
    }
}
