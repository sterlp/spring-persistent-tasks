package org.sterl.spring.persistent_tasks.history.model;

import org.sterl.spring.persistent_tasks.api.TriggerId;
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

@Entity
@Table(name = "SPT_TRIGGERS_HISTORY",
    indexes = {
        @Index(name = "IDX_SPT_TRIGGERS_HISTORY_INSTANCE_ID", columnList = "instance_id"),
        @Index(name = "IDX_SPT_TRIGGERS_HISTORY_TRIGGER_ID", columnList = "trigger_id"),
        @Index(name = "IDX_SPT_TRIGGERS_HISTORY_TASK_NAME", columnList = "task_name"),
        @Index(name = "IDX_SPT_TRIGGERS_HISTORY_STATUS", columnList = "status"),
    }
)
@Data
@NoArgsConstructor
@Builder(toBuilder = true)
@AllArgsConstructor
@EqualsAndHashCode(of = "id")
public class TriggerHistoryEntity {

    @GeneratedValue(strategy = GenerationType.SEQUENCE)
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
    
    public TriggerId getKey() {
        return data.getKey();
    }
}
