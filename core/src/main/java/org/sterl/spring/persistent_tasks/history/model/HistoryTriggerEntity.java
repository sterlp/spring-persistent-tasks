package org.sterl.spring.persistent_tasks.history.model;

import java.time.OffsetDateTime;

import org.sterl.spring.persistent_tasks.api.TriggerKey;
import org.sterl.spring.persistent_tasks.shared.model.HasTrigger;
import org.sterl.spring.persistent_tasks.shared.model.TriggerEntity;

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
 * The history of the {@link TriggerEntity} with the states.
 */
@Entity
@Table(name = "pt_trigger_history", indexes = {
        @Index(name = "idx_pt_trigger_history_instance_id", columnList = "instance_id"),
        @Index(name = "idx_pt_trigger_history_name", columnList = "task_name"),
        @Index(name = "idx_pt_trigger_history_trigger_id", columnList = "trigger_id"),
        @Index(name = "idx_pt_trigger_history_status", columnList = "status"),
        @Index(name = "idx_pt_trigger_history_created_time", columnList = "created_time"),
        @Index(name = "idx_pt_trigger_history_correlation_id", columnList = "correlation_id"),
        @Index(name = "idx_pt_trigger_history_tag", columnList = "tag"),
})
@Data
@NoArgsConstructor
@Builder(toBuilder = true)
@AllArgsConstructor
@EqualsAndHashCode(of = "id")
public class HistoryTriggerEntity implements HasTrigger {

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
    private TriggerEntity data;

    public TriggerKey getKey() {
        return data.getKey();
    }

    public void setCreatedTime(OffsetDateTime time) {
        this.data.setCreatedTime(time);
    }
}
