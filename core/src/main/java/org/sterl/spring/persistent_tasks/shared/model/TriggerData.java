package org.sterl.spring.persistent_tasks.shared.model;

import java.time.Duration;
import java.time.OffsetDateTime;

import org.sterl.spring.persistent_tasks.api.TriggerKey;
import org.sterl.spring.persistent_tasks.api.TriggerStatus;

import jakarta.persistence.AttributeOverride;
import jakarta.persistence.AttributeOverrides;
import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import jakarta.persistence.Embedded;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Lob;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Builder.Default;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

/**
 * Basically a value class for the ability to clone the data more easily
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Embeddable
@ToString(of = {"key", "correlationId", "status", "priority", "executionCount", "createdTime", "runAt", "start", "end"})
@Builder(toBuilder = true)
public class TriggerData {
    
    public void updateRunningDuration() {
        if (start != null && end != null) {
            runningDurationInMs = Duration.between(start, end).toMillis();
        } else {
            runningDurationInMs = null;
        }
    }
    
    @Embedded
    @AttributeOverrides(@AttributeOverride(
            name = "id",
            column = @Column(name = "trigger_id", nullable = false, length = 200, updatable = false)
        )
    )
    private TriggerKey key;

    @Column(nullable = true, updatable = false)
    private String correlationId;

    @Default
    @NotNull
    @Column(nullable = false, updatable = false, name = "created_time")
    private OffsetDateTime createdTime = OffsetDateTime.now();

    @Default
    @NotNull
    @Column(nullable = false)
    private OffsetDateTime runAt = OffsetDateTime.now();

    @Column(name = "start_time")
    private OffsetDateTime start;

    @Column(name = "end_time")
    private OffsetDateTime end;

    @Default
    private int executionCount = 0;

    /** priority, the higher a more priority it will get */
    @Default
    private int priority = 4;

    @Enumerated(EnumType.STRING)
    @Column(length = 20, nullable = false)
    @Default
    private TriggerStatus status = TriggerStatus.WAITING;
    
    private Long runningDurationInMs;

    @Lob
    @Column(name = "state")
    private byte[] state;

    @Column(length = 512)
    private String exceptionName;
    @Lob
    private String lastException;
    
    public TriggerData copy() {
        return this.toBuilder().build();
    }
}
