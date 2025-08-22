package org.sterl.spring.persistent_tasks.history.model;

import java.time.OffsetDateTime;

import org.sterl.spring.persistent_tasks.api.TriggerKey;
import org.sterl.spring.persistent_tasks.shared.model.HasTrigger;
import org.sterl.spring.persistent_tasks.shared.model.TriggerEntity;

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
@Table(name = "pt_completed_triggers", indexes = {
        @Index(name = "idx_pt_completed_triggers_task_name", columnList = "task_name"),
        @Index(name = "idx_pt_completed_triggers_trigger_id", columnList = "trigger_id"),
        @Index(name = "idx_pt_completed_triggers_status", columnList = "status"),
        @Index(name = "idx_pt_completed_triggers_created_time", columnList = "created_time"),
        @Index(name = "idx_pt_completed_triggers_correlation_id", columnList = "correlation_id"),
        @Index(name = "idx_pt_completed_triggers_tag", columnList = "tag"),
})
@Data
@NoArgsConstructor
@Builder(toBuilder = true)
@AllArgsConstructor
@EqualsAndHashCode(of = "id")
public class CompletedTriggerEntity implements HasTrigger {

    @Column(updatable = false)
    @Id
    private Long id;

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
