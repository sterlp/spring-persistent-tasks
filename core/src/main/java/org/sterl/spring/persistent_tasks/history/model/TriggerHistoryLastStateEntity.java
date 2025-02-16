package org.sterl.spring.persistent_tasks.history.model;

import java.time.OffsetDateTime;

import org.sterl.spring.persistent_tasks.api.TriggerKey;
import org.sterl.spring.persistent_tasks.shared.model.HasTriggerData;
import org.sterl.spring.persistent_tasks.shared.model.TriggerData;

import jakarta.persistence.Column;
import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "pt_trigger_history_last_states", indexes = {
        @Index(name = "idx_pt_trigger_history_last_states_task_name", columnList = "task_name"),
        @Index(name = "idx_pt_trigger_history_last_states_trigger_id", columnList = "trigger_id"),
        @Index(name = "idx_pt_trigger_history_last_states_status", columnList = "status"),
        @Index(name = "idx_pt_trigger_history_last_states_created_time", columnList = "created_time"),
        @Index(name = "idx_pt_trigger_history_last_states_correlation_id", columnList = "correlation_id"),
})
@Data
@NoArgsConstructor
@Builder(toBuilder = true)
@AllArgsConstructor
@EqualsAndHashCode(of = "id")
public class TriggerHistoryLastStateEntity implements HasTriggerData {

    @Column(updatable = false)
    @Id
    private Long id;

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
