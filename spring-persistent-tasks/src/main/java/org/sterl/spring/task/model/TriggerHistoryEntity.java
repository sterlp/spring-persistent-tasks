package org.sterl.spring.task.model;

import java.time.OffsetDateTime;

import org.sterl.spring.task.api.TriggerId;

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
import lombok.Builder.Default;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "TASK_TRIGGERS_HISTORY", 
    indexes = {
        @Index(name = "IDX_TASK_TRIGGERS_HISTORY_CREATE_DATE", columnList = "create_date"),
        @Index(name = "IDX_TASK_TRIGGERS_HISTORY_TASK_ID", columnList = "task_id"),
        @Index(name = "IDX_TASK_TRIGGERS_HISTORY_NAME", columnList = "name"),
        @Index(name = "IDX_TASK_TRIGGERS_HISTORY_PRIORITY", columnList = "priority"),
        @Index(name = "IDX_TASK_TRIGGERS_HISTORY_TIME", columnList = "trigger_time"),
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
    private BaseTriggerData data;
    
    @Default
    private OffsetDateTime createDate = OffsetDateTime.now();
}
