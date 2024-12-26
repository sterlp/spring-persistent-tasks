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
@Table(name = "SPT_TRIGGER_HISTORY_LAST_STATES",
    indexes = {
        @Index(name = "IDX_SPT_TRIGGER_HISTORY_LAST_STATES_TASK_NAME", columnList = "task_name"),
        @Index(name = "IDX_SPT_TRIGGER_HISTORY_LAST_STATES_TRIGGER_ID", columnList = "trigger_id"),
        @Index(name = "IDX_SPT_TRIGGER_HISTORY_LAST_STATES_STATUS", columnList = "status"),
        @Index(name = "IDX_SPT_TRIGGER_HISTORY_LAST_STATES_CREATED_TIME", columnList = "created_time"),
    }
)
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
