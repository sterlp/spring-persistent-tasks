package org.sterl.spring.persistent_tasks.history.model;

import java.time.OffsetDateTime;

import org.apache.commons.lang3.StringUtils;
import org.sterl.spring.persistent_tasks.api.TriggerKey;
import org.sterl.spring.persistent_tasks.api.TriggerStatus;
import org.sterl.spring.persistent_tasks.shared.model.TriggerEntity;

import jakarta.persistence.AttributeOverride;
import jakarta.persistence.AttributeOverrides;
import jakarta.persistence.Column;
import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
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

/**
 * The history of the {@link TriggerEntity} with the states.
 */
@Entity
@Table(name = "pt_trigger_history", indexes = {
    @Index(name = "idx_pt_trigger_history_instance_id", columnList = "instance_id"),
})
@Data
@NoArgsConstructor
@Builder(toBuilder = true)
@AllArgsConstructor
@EqualsAndHashCode(of = "id")
public class HistoryTriggerEntity {

    @GeneratedValue(generator = "seq_pt_trigger_history", strategy = GenerationType.SEQUENCE)
    @Column(updatable = false)
    @Id
    private Long id;
    
    @Enumerated(EnumType.STRING)
    @Column(updatable = false, nullable = false)
    private TriggerStatus status;

    /**
     * The original ID of this trigger in case grouping is needed
     * as for each trigger multiple history entries are added.
     */
    @Column(name = "instance_id", updatable = false)
    private Long instanceId;

    @Embedded
    @AttributeOverrides(@AttributeOverride(
            name = "id",
            column = @Column(name = "trigger_id", nullable = false, length = 200, updatable = false)
        )
    )
    private TriggerKey key;
    
    @Default
    @NotNull
    @Column(nullable = false, updatable = false, name = "created_time")
    private OffsetDateTime createdTime = OffsetDateTime.now();

    @Column(name = "start_time", updatable = false)
    private OffsetDateTime start;

    @Column(updatable = false)
    private int executionCount;

    @Column(length = 200, updatable = false)
    private String message;
    
    
    public static HistoryTriggerEntity from(TriggerEntity data) {
        var result = new HistoryTriggerEntity();
        result.setExecutionCount(data.getExecutionCount());
        result.setKey(data.getKey());
        
        var msg = data.getExceptionName();
        if (data.getLastException() != null) msg = data.getLastException();
        result.setMessage(StringUtils.substring(msg, 0, 200));

        result.setStart(data.getStart());
        result.setStatus(data.getStatus());
        return result;
    }

}
