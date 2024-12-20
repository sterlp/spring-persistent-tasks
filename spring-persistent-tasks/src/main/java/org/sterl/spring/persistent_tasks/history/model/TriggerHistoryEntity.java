package org.sterl.spring.persistent_tasks.history.model;

import org.sterl.spring.persistent_tasks.api.TriggerId;
import org.sterl.spring.persistent_tasks.shared.model.TriggerData;

import jakarta.persistence.AttributeOverride;
import jakarta.persistence.AttributeOverrides;
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
@Table(name = "SPT_TASK_TRIGGERS_HISTORY",
    indexes = {
        @Index(name = "IDX_TASK_TRIGGERS_HISTORY_CREATE_DATE", columnList = "created_time"),
        @Index(name = "IDX_TASK_TRIGGERS_HISTORY_TASK_ID", columnList = "task_id"),
        @Index(name = "IDX_TASK_TRIGGERS_HISTORY_NAME", columnList = "name"),
        @Index(name = "IDX_TASK_TRIGGERS_HISTORY_PRIORITY", columnList = "priority"),
        @Index(name = "IDX_TASK_TRIGGERS_HISTORY_RUN_AT", columnList = "run_at"),
        @Index(name = "IDX_TASK_TRIGGERS_HISTORY_STATUS", columnList = "status"),
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

    @Embedded
    @AttributeOverrides(@AttributeOverride(
            name = "id",
            column = @Column(name = "task_id", nullable = false))
    )
    @NotNull
    private TriggerId triggerId;

    @Embedded
    @NotNull
    private TriggerData data;
}
